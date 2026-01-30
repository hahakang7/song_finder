package hyun9.song_finder.controller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class ArtistController {

    private final YoutubeService youtubeService;
    private final OAuth2AuthorizedClientService clientService;
    private final DumpService dumpService;

    @GetMapping("/compare")
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

        String userId = principal.getName();

        // 1) 채널 uploads playlistId
        String uploadsPlaylistId = youtubeService.getUploadsPlaylistId(channelId);
        if (uploadsPlaylistId == null) {
            model.addAttribute("error", "채널의 업로드 재생목록을 찾지 못했습니다.");
            return "error";
        }

        // 2) 아티스트 영상 수집
        Set<String> artistVideoIds =
                youtubeService.getAllVideoIdsInPlaylist(accessToken, uploadsPlaylistId);

        List<Map<String, Object>> artistVideosDetailed =
                youtubeService.getVideosByIdsWithDetails(
                        accessToken, new ArrayList<>(artistVideoIds));

        // 3) Topic 채널 우선 곡 수집
        String artistName = (String)
                ((Map<String, Object>) artistVideosDetailed.get(0)
                        .get("snippet"))
                        .get("channelTitle");

        List<Map<String, Object>> artistSongsDetailed =
                youtubeService.loadSongsFromTopicChannel(artistName, accessToken);

        // fallback
        if (artistSongsDetailed.isEmpty()) {
            artistSongsDetailed =
                    youtubeService.filterLikelySongs(artistVideosDetailed);
        }

        // 4) 플레이리스트 곡 제목 Set
        Set<String> playlistVideoIds =
                youtubeService.getAllVideoIdsInPlaylist(accessToken, playlistId);

        List<Map<String, Object>> playlistVideosDetailed =
                youtubeService.getVideosByIdsWithDetails(
                        accessToken, new ArrayList<>(playlistVideoIds));

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

        // 5) Dump 목록 조회
        Set<String> dumpedTitles =
                dumpService.getDumpedTitles(userId, channelId);

        // 6) 결과 3분기
        List<Map<String, Object>> dumpedVideos = new ArrayList<>();
        List<Map<String, Object>> missingVideos = new ArrayList<>();
        List<Map<String, Object>> containedVideos = new ArrayList<>();

        for (Map<String, Object> v : artistSongsDetailed) {
            Map<String, Object> snippet = (Map<String, Object>) v.get("snippet");
            if (snippet == null) continue;

            String rawTitle = (String) snippet.get("title");
            String channelTitle = (String) snippet.get("channelTitle");

            String normalized =
                    youtubeService.normalizeSongTitle(rawTitle, channelTitle);

            if (normalized.isEmpty()) continue;

            // 표시용
            snippet.put("normalizedTitle", normalized);

            if (dumpedTitles.contains(normalized)) {
                dumpedVideos.add(v);
            } else if (playlistSongTitles.contains(normalized)) {
                containedVideos.add(v);
            } else {
                missingVideos.add(v);
            }
        }

        // 7) model 전달
        model.addAttribute("dumpedVideos", dumpedVideos);
        model.addAttribute("missingVideos", missingVideos);
        model.addAttribute("containedVideos", containedVideos);

        model.addAttribute("dumpedCount", dumpedVideos.size());
        model.addAttribute("missingCount", missingVideos.size());
        model.addAttribute("containedCount", containedVideos.size());

        model.addAttribute("channelId", channelId);
        model.addAttribute("playlistId", playlistId);

        return "compare-result";
    }


    @PostMapping("/dump")
    public String dumpSong(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("channelId") String channelId,
            @RequestParam("playlistId") String playlistId,
            @RequestParam("normalizedTitle") String normalizedTitle
    ) {
        String userId = principal.getName();

        dumpService.dumpSong(userId, channelId, normalizedTitle);

        // 다시 비교 화면으로
        return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId;
    }







}
