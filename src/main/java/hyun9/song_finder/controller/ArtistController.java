package hyun9.song_finder.controller;

import hyun9.song_finder.domain.ArtistSong;
import hyun9.song_finder.domain.PlaylistSong;
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

        if (artistSubscribed && playlistSubscribed) {
            return compareWithDb(userId, channelId, playlistId, model);
        }

        return compareWithApi(principal, channelId, playlistId, model);
    }

    /**
     * ===============================
     * DB 기반 Warm Compare
     * ===============================
     */
    private String compareWithDb(
            String userId,
            String channelId,
            String playlistId,
            Model model
    ) {

        // 1) DB에서 곡 제목 로딩
        Set<String> artistTitles =
                artistSongRepository.findByChannelId(channelId)
                        .stream()
                        .map(ArtistSong::getNormalizedTitle)
                        .collect(Collectors.toSet());

        Set<String> playlistTitles =
                playlistSongRepository.findByPlaylistId(playlistId)
                        .stream()
                        .map(PlaylistSong::getNormalizedTitle)
                        .collect(Collectors.toSet());

        Set<String> dumpedTitles =
                dumpService.getDumpedTitles(userId, channelId);

        // 2) 분기
        List<String> dumped = new ArrayList<>();
        List<String> contained = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String title : artistTitles) {
            if (dumpedTitles.contains(title)) {
                dumped.add(title);
            } else if (playlistTitles.contains(title)) {
                contained.add(title);
            } else {
                missing.add(title);
            }
        }

        // 3) view 전달 (DB 모드는 제목만)
        model.addAttribute("dumpedTitles", dumped);
        model.addAttribute("containedTitles", contained);
        model.addAttribute("missingTitles", missing);

        model.addAttribute("dumpedCount", dumped.size());
        model.addAttribute("containedCount", contained.size());
        model.addAttribute("missingCount", missing.size());

        model.addAttribute("mode", "DB");

        return "compare-result";
    }

    /**
     * ===============================
     * API 기반 Cold Compare (기존 로직)
     * ===============================
     */
    private String compareWithApi(
            OAuth2User principal,
            String channelId,
            String playlistId,
            Model model
    ) {

        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        String userId = principal.getName();

        // 1) 아티스트 uploads
        String uploadsPlaylistId =
                youtubeService.getUploadsPlaylistId(channelId);

        Set<String> artistVideoIds =
                youtubeService.getAllVideoIdsInPlaylist(accessToken, uploadsPlaylistId);

        List<Map<String, Object>> artistVideosDetailed =
                youtubeService.getVideosByIdsWithDetails(
                        accessToken, new ArrayList<>(artistVideoIds));

        List<Map<String, Object>> artistSongs =
                youtubeService.filterLikelySongs(artistVideosDetailed);

        // 2) 플레이리스트
        Set<String> playlistVideoIds =
                youtubeService.getAllVideoIdsInPlaylist(accessToken, playlistId);

        List<Map<String, Object>> playlistVideos =
                youtubeService.getVideosByIdsWithDetails(
                        accessToken, new ArrayList<>(playlistVideoIds));

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

        // 3) 분기
        List<Map<String, Object>> dumped = new ArrayList<>();
        List<Map<String, Object>> contained = new ArrayList<>();
        List<Map<String, Object>> missing = new ArrayList<>();

        for (Map<String, Object> v : artistSongs) {
            Map<String, Object> snippet = (Map<String, Object>) v.get("snippet");
            if (snippet == null) continue;

            String rawTitle = (String) snippet.get("title");
            String channelTitle = (String) snippet.get("channelTitle");

            String normalized =
                    youtubeService.normalizeSongTitle(rawTitle, channelTitle);

            if (normalized.isBlank()) continue;

            snippet.put("normalizedTitle", normalized);

            if (dumpedTitles.contains(normalized)) {
                dumped.add(v);
            } else if (playlistTitles.contains(normalized)) {
                contained.add(v);
            } else {
                missing.add(v);
            }
        }

        // 4) view 전달
        model.addAttribute("dumpedVideos", dumped);
        model.addAttribute("containedVideos", contained);
        model.addAttribute("missingVideos", missing);

        model.addAttribute("dumpedCount", dumped.size());
        model.addAttribute("containedCount", contained.size());
        model.addAttribute("missingCount", missing.size());

        model.addAttribute("mode", "API");

        return "compare-result";
    }
}
