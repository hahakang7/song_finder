package hyun9.song_finder.controller;

import hyun9.song_finder.service.AuthStateService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import hyun9.song_finder.domain.SubscribedPlaylist;
import hyun9.song_finder.repository.SubscribedPlaylistRepository;
import hyun9.song_finder.service.DummyAuthService;
import hyun9.song_finder.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PlaylistController {

    private final AuthStateService authStateService;

    @GetMapping("/playlists")
    public String getUserPlaylists(@RequestParam(required = false) String channelId,
                                   Model model,
                                   HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);
        model.addAttribute("selectedChannelId", channelId);

        model.addAttribute("playlists", List.of(
                Map.of("id", "pl-favorite", "title", "내 최애곡", "subscribed", true, "lastSyncedAt", "2026-02-13 21:10"),
                Map.of("id", "pl-drive", "title", "드라이브 노래", "subscribed", true, "lastSyncedAt", "2026-02-09 18:20"),
                Map.of("id", "pl-study", "title", "집중용 플레이리스트", "subscribed", false, "lastSyncedAt", "-")
        ));
    private final YoutubeService youtubeService;
    private final DummyAuthService dummyAuthService;
    private final SubscribedPlaylistRepository subscribedPlaylistRepository;

    @GetMapping("/playlists")
    public String getUserPlaylists(@AuthenticationPrincipal OAuth2User principal,
                                   @RequestParam(required = false) String pageToken,
                                   Model model) {

        String accessToken = dummyAuthService.resolveAccessToken(principal);

        Map<String, Object> result = youtubeService.getPaginatedPlaylists(accessToken, pageToken);
        model.addAttribute("playlists", result.get("playlists"));
        model.addAttribute("nextPageToken", result.get("nextPageToken"));
        model.addAttribute("prevPageToken", result.get("prevPageToken"));

        String userId = dummyAuthService.resolveUserId(principal);

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

    @GetMapping("/playlist/{id}")
    public String getPlaylistItems(@PathVariable String id, Model model, HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);
        model.addAttribute("id", id);

        model.addAttribute("playlistItems", List.of(
                Map.of("title", "Ditto"),
                Map.of("title", "밤편지"),
                Map.of("title", "Attention")
        ));


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


    @GetMapping("/playlist/{id}")
    public String getPlaylistItems(@AuthenticationPrincipal OAuth2User principal,
                                   @PathVariable String id,
                                   @RequestParam(defaultValue = "") String pageToken,
                                   Model model) {
        String accessToken = dummyAuthService.resolveAccessToken(principal);

        Map<String, Object> result = youtubeService.getPlaylistItems(accessToken, id, pageToken);
        model.addAttribute("playlistItems", result.get("items"));
        model.addAttribute("nextPageToken", result.get("nextPageToken"));
        model.addAttribute("prevPageToken", result.get("prevPageToken"));

        return "playlist-detail";
    }
}
