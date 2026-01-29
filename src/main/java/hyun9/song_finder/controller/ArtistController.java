package hyun9.song_finder.controller;

import hyun9.song_finder.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class ArtistController {

    private final YoutubeService youtubeService;
    private final OAuth2AuthorizedClientService clientService;


    @PostMapping("/compare")
    public String compareChannelWithPlaylist(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("channelId") String channelId,
            @RequestParam("playlistId") String playlistId,
            Model model
    ) {

        // 0) access token
        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        // 1) 채널 uploads playlistId
        String uploadsPlaylistId = youtubeService.getUploadsPlaylistId(channelId);
        if (uploadsPlaylistId == null) {
            model.addAttribute("error", "채널의 업로드 재생목록을 찾지 못했습니다.");
            return "error";
        }

        // 2) 아티스트 uploads videoId 수집
        Set<String> artistVideoIds =
                youtubeService.getAllVideoIdsInPlaylist(accessToken, uploadsPlaylistId);

        if (artistVideoIds.isEmpty()) {
            model.addAttribute("missingVideos", List.of());
            model.addAttribute("missingCount", 0);
            model.addAttribute("note", "채널 업로드 목록에서 영상이 발견되지 않았습니다.");
            return "compare-result";
        }

        // 3) 아티스트 영상 상세 조회
        List<Map<String, Object>> artistVideosDetailed =
                youtubeService.getVideosByIdsWithDetails(
                        accessToken, new ArrayList<>(artistVideoIds));

        // 4) 곡 후보만 필터링
        // 아티스트명은 snippet.channelTitle 기준으로 이미 확보 가능
        String artistName = (String)
                ((Map<String, Object>) artistVideosDetailed.get(0)
                        .get("snippet"))
                        .get("channelTitle");

        // 4-1) Topic 채널 시도
        List<Map<String, Object>> artistSongsDetailed =
                youtubeService.loadSongsFromTopicChannel(
                        artistName,
                        accessToken
                );

        // 4-2) Topic이 없으면 공식 채널 fallback
        if (artistSongsDetailed.isEmpty()) {
            artistSongsDetailed =
                    youtubeService.filterLikelySongs(artistVideosDetailed);
        }

        // 5) 플레이리스트 videoId 수집
        Set<String> playlistVideoIds =
                youtubeService.getAllVideoIdsInPlaylist(accessToken, playlistId);

        List<Map<String, Object>> playlistVideosDetailed =
                youtubeService.getVideosByIdsWithDetails(
                        accessToken, new ArrayList<>(playlistVideoIds));

        // 6) 플레이리스트 정제된 곡 제목 Set
        Set<String> playlistSongTitles = new HashSet<>();

        for (Map<String, Object> v : playlistVideosDetailed) {
            Map<String, Object> snippet = (Map<String, Object>) v.get("snippet");
            if (snippet == null) continue;

            String rawTitle = (String) snippet.get("title");
            String channelTitle = (String) snippet.get("channelTitle");

            String normalized =
                    youtubeService.normalizeSongTitle(rawTitle, channelTitle);

            if (!normalized.isEmpty()) {
                playlistSongTitles.add(normalized);
            }
        }

        // 7) 아티스트 곡 vs 플레이리스트 비교
        List<Map<String, Object>> missingVideos = new ArrayList<>();
        List<Map<String, Object>> containedVideos = new ArrayList<>();

        for (Map<String, Object> v : artistSongsDetailed) {
            Map<String, Object> snippet = (Map<String, Object>) v.get("snippet");
            if (snippet == null) continue;

            String rawTitle = (String) snippet.get("title");
            String channelTitle = (String) snippet.get("channelTitle");

            String normalizedArtistTitle =
                    youtubeService.normalizeSongTitle(rawTitle, channelTitle);

            boolean contained = false;
            for (String plTitle : playlistSongTitles) {
                if (normalizedArtistTitle.contains(plTitle)) {
                    contained = true;
                    break;
                }
            }

            snippet.put("normalizedTitle", normalizedArtistTitle);

            if (contained) {
                containedVideos.add(v);
            } else {
                missingVideos.add(v);
            }
        }


        model.addAttribute("missingVideos", missingVideos);
        model.addAttribute("containedVideos", containedVideos);

        model.addAttribute("missingCount", missingVideos.size());
        model.addAttribute("containedCount", containedVideos.size());


        return "compare-result";
    }







}
