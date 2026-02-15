package hyun9.song_finder.controller;

import hyun9.song_finder.domain.SubscribedPlaylist;
import hyun9.song_finder.repository.SubscribedPlaylistRepository;
import hyun9.song_finder.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PlaylistController {


    private final YoutubeService youtubeService;
    private final OAuth2AuthorizedClientService clientService;
    private final SubscribedPlaylistRepository subscribedPlaylistRepository;

    //
    @GetMapping("/playlists")
    public String getUserPlaylists(@AuthenticationPrincipal OAuth2User principal,
                                   @RequestParam(required = false) String pageToken,
                                   Model model) {

        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        // String pageToken을 그대로 넘긴다
        Map<String, Object> result = youtubeService.getPaginatedPlaylists(accessToken, pageToken);
        model.addAttribute("playlists", result.get("playlists"));
        model.addAttribute("nextPageToken", result.get("nextPageToken"));
        model.addAttribute("prevPageToken", result.get("prevPageToken"));

        String userId = principal.getName();

        List<SubscribedPlaylist> subs = subscribedPlaylistRepository.findByUserId(userId);

        Set<String> subscribedPlaylistIds =
                subs.stream().map(SubscribedPlaylist::getPlaylistId).collect(Collectors.toSet());

        Map<String, LocalDateTime> playlistLastSyncedMap =
                subs.stream().collect(Collectors.toMap(
                        SubscribedPlaylist::getPlaylistId,
                        SubscribedPlaylist::getLastSyncedAt,
                        (a, b) -> a
                ));

        model.addAttribute("subscribedPlaylistIds", subscribedPlaylistIds);
        model.addAttribute("playlistLastSyncedMap", playlistLastSyncedMap);


        return "playlists";
    }



    //
    public Map<String, Object> fetchPlaylistsFromYouTube(String accessToken, String pageToken) {
        String baseUrl = "https://www.googleapis.com/youtube/v3/playlists?part=snippet&mine=true&maxResults=5";
        if (pageToken != null && !pageToken.isEmpty()) {
            baseUrl += "&pageToken=" + pageToken;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, Map.class);

        Map<String, Object> responseBody = response.getBody();

        Map<String, Object> result = new HashMap<>();
        result.put("items", responseBody.get("items"));
        result.put("nextPageToken", responseBody.get("nextPageToken"));
        result.put("prevPageToken", responseBody.get("prevPageToken"));

        return result;
    }


    //플레이리스트 들어가면 내용
    @GetMapping("/playlist/{id}")
    public String getPlaylistItems(@AuthenticationPrincipal OAuth2User principal,
                                   @PathVariable String id,
                                   @RequestParam(defaultValue = "") String pageToken,
                                   Model model) {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        // 특정 플레이리스트의 영상 목록 페이지네이션
        Map<String, Object> result = youtubeService.getPlaylistItems(accessToken, id, pageToken);
        model.addAttribute("playlistItems", result.get("items"));
        model.addAttribute("nextPageToken", result.get("nextPageToken"));
        model.addAttribute("prevPageToken", result.get("prevPageToken"));

        return "playlist-detail";
    }

    public List<Map<String, Object>> fetchPlaylistItems(String accessToken, String playlistId) {
        String url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" +
                playlistId + "&maxResults=50";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        return (List<Map<String, Object>>) response.getBody().get("items");
    }


}

