package hyun9.song_finder.controller;

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

    //검색기능은 추후 개발 잠정폐지
    /*

    // 1단계: 검색폼 렌더링
    @GetMapping("/artist")
    public String showArtistSearchPage() {
        return "artist-search";
    }



    @GetMapping("/artist/search")
    public String handleArtistSearch(@RequestParam("artistName") String artistName, Model model) {
        List<Map<String, Object>> channels = youtubeService.searchChannelsByArtist(artistName);

        model.addAttribute("artistName", artistName);
        model.addAttribute("channels", channels);

        return "artis-result";
    }
     */


    @PostMapping("/compare")
    public String compareChannelWithPlaylist(@AuthenticationPrincipal OAuth2User principal,
                                             @RequestParam("channelId") String channelId,
                                             @RequestParam("playlistId") String playlistId,
                                             Model model) {

        // 0) access token
        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        // 1) 채널의 uploads playlistId 얻기
        String uploadsPlaylistId = youtubeService.getUploadsPlaylistId(channelId);
        if (uploadsPlaylistId == null) {
            model.addAttribute("error", "채널의 업로드 재생목록을 찾지 못했습니다.");
            return "error";
        }

        // 2) uploads playlist에서 videoId 전부 수집 (페이지네이션 포함)
        Set<String> rawArtistVideoIds =
                youtubeService.getAllVideoIdsInPlaylist(accessToken, uploadsPlaylistId);

        // uploads가 비었으면 바로 종료
        if (rawArtistVideoIds.isEmpty()) {
            model.addAttribute("missingVideos", List.of());
            model.addAttribute("missingCount", 0);
            model.addAttribute("note", "채널 업로드 목록에서 영상이 발견되지 않았습니다.");
            return "compare-result";
        }

        // 3) videos.list로 (snippet + contentDetails) 가져오기 (배치 50개씩)
        List<String> rawList = new ArrayList<>(rawArtistVideoIds);
        List<Map<String, Object>> artistVideosDetailed =
                youtubeService.getVideosByIdsWithDetails(accessToken, rawList);

        // 4) 정제: 곡 후보만 남기기 (shorts/잡영상 제거)
        List<Map<String, Object>> artistSongsDetailed =
                youtubeService.filterLikelySongs(artistVideosDetailed);

        // 5) 정제된 곡 후보들의 videoId set 만들기
        Set<String> artistSongIds = new HashSet<>();
        for (Map<String, Object> v : artistSongsDetailed) {
            String vid = (String) v.get("id");
            if (vid != null) artistSongIds.add(vid);
        }

        // 6) 사용자가 선택한 플레이리스트의 videoId 전부 수집 (페이지네이션 포함)
        Set<String> userPlaylistVideoIds =
                youtubeService.getAllVideoIdsInPlaylist(accessToken, playlistId);

        // 7) 차집합: (아티스트 곡 후보) - (사용자 플레이리스트)
        Set<String> missingIds = new HashSet<>(artistSongIds);
        missingIds.removeAll(userPlaylistVideoIds);

        // 8) missing 상세는 이미 artistSongsDetailed에 있으니 거기서 골라내기 (추가 API 호출 없음)
        List<Map<String, Object>> missingVideos = new ArrayList<>();
        for (Map<String, Object> v : artistSongsDetailed) {
            String vid = (String) v.get("id");
            if (vid != null && missingIds.contains(vid)) {
                missingVideos.add(v);
            }
        }

        model.addAttribute("missingVideos", missingVideos);
        model.addAttribute("missingCount", missingVideos.size());

        // (선택) 디버그용: 정제 전/후 개수 비교
        model.addAttribute("rawCount", rawArtistVideoIds.size());
        model.addAttribute("filteredCount", artistSongsDetailed.size());

        return "compare-result";
    }






}
