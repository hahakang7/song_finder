package hyun9.song_finder.controller;

import hyun9.song_finder.service.AuthStateService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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

    @GetMapping({"/artist/link", "/artists/register"})
    public String showChannelInput(Model model, HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);
        return "channel-input";
    }

    @PostMapping({"/artist/link", "/artists/register"})
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

        return "channel-registered";
    }
}
