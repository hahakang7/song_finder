package hyun9.song_finder.controller;

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
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Controller
public class PlaylistController {

    private final OAuth2AuthorizedClientService clientService;

    public PlaylistController(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/playlists")
    public String getPlaylists(@AuthenticationPrincipal OAuth2User principal, Model model) {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                "google", principal.getName());

        String accessToken = client.getAccessToken().getTokenValue();

        List<Map<String, Object>> playlists = fetchPlaylistsFromYouTube(accessToken);
        model.addAttribute("playlists", playlists);

        return "playlists"; // templates/playlists.html
    }

    private List<Map<String, Object>> fetchPlaylistsFromYouTube(String accessToken) {
        String url = "https://www.googleapis.com/youtube/v3/playlists?part=snippet&mine=true&maxResults=25";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        return (List<Map<String, Object>>) response.getBody().get("items");
    }
}

