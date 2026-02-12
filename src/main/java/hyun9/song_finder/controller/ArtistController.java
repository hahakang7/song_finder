package hyun9.song_finder.controller;

import hyun9.song_finder.domain.ArtistSong;
import hyun9.song_finder.domain.PlaylistSong;
import hyun9.song_finder.dto.CompareItemDTO;
import hyun9.song_finder.dto.CompareStatus;
import hyun9.song_finder.repository.ArtistSongRepository;
import hyun9.song_finder.repository.PlaylistSongRepository;
import hyun9.song_finder.repository.SubscribedArtistRepository;
import hyun9.song_finder.repository.SubscribedPlaylistRepository;
import hyun9.song_finder.service.DumpService;
import hyun9.song_finder.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ArtistController {

    private final YoutubeService youtubeService;
    private final DumpService dumpService;
    private final OAuth2AuthorizedClientService clientService;

    private final SubscribedArtistRepository subscribedArtistRepository;
    private final SubscribedPlaylistRepository subscribedPlaylistRepository;
    private final ArtistSongRepository artistSongRepository;
    private final PlaylistSongRepository playlistSongRepository;

    /**
     * compare 진입점
     * - DB에 구독 데이터 있으면 DB compare
     * - 없으면 API compare
     */
    @GetMapping("/compare")
    public String compare(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("channelId") String channelId,
            @RequestParam("playlistId") String playlistId,
            Model model
    ) {

        String userId = principal.getName();

        boolean artistSubscribed =
                subscribedArtistRepository.existsByUserIdAndChannelId(userId, channelId);

        boolean playlistSubscribed =
                subscribedPlaylistRepository.existsByUserIdAndPlaylistId(userId, playlistId);

        List<CompareItemDTO> items;

        //사용자 플리, 아티스트 둘다 구독했으면 DB모드
        String compareMode = (artistSubscribed && playlistSubscribed) ? "DB" : "API";

        if (artistSubscribed && playlistSubscribed) {
            items = compareWithDb(userId, channelId, playlistId);
        } else {
            items = compareWithApi(principal, userId, channelId, playlistId);
        }

        model.addAttribute("items", items);

        model.addAttribute("compareMode", compareMode);
        model.addAttribute("isFastMode", "DB".equals(compareMode));

        model.addAttribute("channelId", channelId);
        model.addAttribute("playlistId", playlistId);

        model.addAttribute("artistSubscribed", artistSubscribed);
        model.addAttribute("playlistSubscribed", playlistSubscribed);


        // (선택) 카운트
        model.addAttribute("dumpedCount",
                items.stream().filter(i -> i.getStatus() == CompareStatus.DUMPED).count());
        model.addAttribute("missingCount",
                items.stream().filter(i -> i.getStatus() == CompareStatus.MISSING).count());
        model.addAttribute("containedCount",
                items.stream().filter(i -> i.getStatus() == CompareStatus.CONTAINED).count());

        return "compare-result";
    }


    /**
     * ===============================
     * API 기반 Cold Compare (기존 로직)
     * ===============================
     */
    private List<CompareItemDTO> compareWithApi(
            OAuth2User principal,
            String userId,
            String channelId,
            String playlistId
    ) {

        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        // 1) 아티스트 영상 수집
        String uploadsPlaylistId =
                youtubeService.getUploadsPlaylistId(channelId);

        Set<String> artistVideoIds =
                youtubeService.getAllVideoIdsInPlaylist(accessToken, uploadsPlaylistId);

        List<Map<String, Object>> artistVideosDetailed =
                youtubeService.getVideosByIdsWithDetails(
                        accessToken,
                        new ArrayList<>(artistVideoIds)
                );

        List<Map<String, Object>> artistSongs =
                youtubeService.filterLikelySongs(artistVideosDetailed);

        // 2) 플레이리스트 title set
        Set<String> playlistVideoIds =
                youtubeService.getAllVideoIdsInPlaylist(accessToken, playlistId);

        List<Map<String, Object>> playlistVideos =
                youtubeService.getVideosByIdsWithDetails(
                        accessToken,
                        new ArrayList<>(playlistVideoIds)
                );

        Set<String> playlistTitles = new HashSet<>();
        for (Map<String, Object> v : playlistVideos) {
            Map<String, Object> snippet = (Map<String, Object>) v.get("snippet");
            if (snippet == null) continue;

            String rawTitle = (String) snippet.get("title");
            String channelTitle = (String) snippet.get("channelTitle");

            String normalized =
                    youtubeService.normalizeSongTitle(rawTitle, channelTitle);

            if (!normalized.isBlank()) {
                playlistTitles.add(normalized);
            }
        }

        Set<String> dumpedTitles =
                dumpService.getDumpedTitles(userId, channelId);

        // 3) artist songs → DTO
        Map<String, String> artistMap = new LinkedHashMap<>();

        for (Map<String, Object> v : artistSongs) {
            Map<String, Object> snippet = (Map<String, Object>) v.get("snippet");
            if (snippet == null) continue;

            String rawTitle = (String) snippet.get("title");
            String channelTitle = (String) snippet.get("channelTitle");

            String normalized =
                    youtubeService.normalizeSongTitle(rawTitle, channelTitle);
            if (normalized.isBlank()) continue;

            String thumb =
                    youtubeService.extractDefaultThumbnailUrl(snippet);

            artistMap.putIfAbsent(normalized, thumb);
        }

        List<CompareItemDTO> items = new ArrayList<>(artistMap.size());

        for (Map.Entry<String, String> e : artistMap.entrySet()) {
            String title = e.getKey();
            String thumb = e.getValue();

            CompareStatus status;
            if (dumpedTitles.contains(title)) {
                status = CompareStatus.DUMPED;
            } else if (playlistTitles.contains(title)) {
                status = CompareStatus.CONTAINED;
            } else {
                status = CompareStatus.MISSING;
            }

            items.add(new CompareItemDTO(title, thumb, status));
        }

        return items;
    }


    private List<CompareItemDTO> compareWithDb(
            String userId,
            String channelId,
            String playlistId
    ) {

        // 1) artist: normalizedTitle -> thumbnailUrl
        Map<String, String> artistMap =
                artistSongRepository.findByChannelId(channelId)
                        .stream()
                        .collect(Collectors.toMap(
                                ArtistSong::getNormalizedTitle,
                                ArtistSong::getThumbnailUrl,
                                (a, b) -> a
                        ));

        // 2) playlist title set
        Set<String> playlistTitles =
                playlistSongRepository.findByUserIdAndPlaylistId(userId, playlistId)
                        .stream()
                        .map(PlaylistSong::getNormalizedTitle)
                        .collect(Collectors.toSet());

        // 3) dump set
        Set<String> dumpedTitles =
                dumpService.getDumpedTitles(userId, channelId);

        // 4) 조립
        List<CompareItemDTO> items = new ArrayList<>(artistMap.size());

        for (Map.Entry<String, String> e : artistMap.entrySet()) {
            String title = e.getKey();
            String thumb = e.getValue();

            CompareStatus status;
            if (dumpedTitles.contains(title)) {
                status = CompareStatus.DUMPED;
            } else if (playlistTitles.contains(title)) {
                status = CompareStatus.CONTAINED;
            } else {
                status = CompareStatus.MISSING;
            }

            items.add(new CompareItemDTO(title, thumb, status));
        }

        return items;
    }
}
