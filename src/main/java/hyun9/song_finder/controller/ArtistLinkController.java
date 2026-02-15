package hyun9.song_finder.controller;

import hyun9.song_finder.service.AuthStateService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import hyun9.song_finder.service.DummyAuthService;
import hyun9.song_finder.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ArtistLinkController {

    private final AuthStateService authStateService;
    private final YoutubeService youtubeService;
    private final DummyAuthService dummyAuthService;

    @GetMapping("/artist/link")
    public String showChannelInput(Model model, HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);
        return "channel-input";
    }

    @PostMapping("/artist/link")
    public String handleChannelLink(@RequestParam("channelUrl") String channelUrl,
                                    Model model,
                                    HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);

        if (!isAuthed) {
            model.addAttribute("showLoginModal", true);
            model.addAttribute("error", "로그인 후 아티스트를 등록할 수 있습니다.");
            return "channel-input";
        }

        model.addAttribute("channelId", "artist-from-link");
        model.addAttribute("artistName", "Dummy Artist");
        model.addAttribute("artistThumbnailUrl", "https://placehold.co/88x88");
        model.addAttribute("channelInfo", Map.of("channelUrl", channelUrl));
        model.addAttribute("playlists", List.of(
                Map.of("id", "pl-favorite", "title", "내 최애곡"),
                Map.of("id", "pl-drive", "title", "드라이브 노래")
        ));
        String accessToken = dummyAuthService.resolveAccessToken(principal);

        Map<String, Object> playlistResult =
                youtubeService.getPaginatedPlaylists(accessToken, null);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> playlists =
                (List<Map<String, Object>>) playlistResult.get("playlists");

        String artistName = null;
        Map<String, Object> snippet = (Map<String, Object>) channel.get("snippet");
        if (snippet != null) {
            artistName = (String) snippet.get("title");
        }
        model.addAttribute("artistName", artistName);

        String artistThumbnailUrl = null;
        if (snippet != null) {
            Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
            if (thumbnails != null) {
                Map<String, Object> thumb =
                        (Map<String, Object>) thumbnails.getOrDefault("default",
                                thumbnails.getOrDefault("medium", thumbnails.get("high")));

                if (thumb != null) {
                    artistThumbnailUrl = (String) thumb.get("url");
                }
            }
        }
        model.addAttribute("artistThumbnailUrl", artistThumbnailUrl);

        model.addAttribute("channelId", channelId);
        model.addAttribute("channelInfo", channel);
        model.addAttribute("playlists", playlists);

        return "channel-registered";
    }
}
