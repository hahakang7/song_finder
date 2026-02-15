package hyun9.song_finder.controller;

import hyun9.song_finder.service.AuthStateService;
import hyun9.song_finder.service.DataContextService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequiredArgsConstructor
public class ArtistLinkController {

    private final AuthStateService authStateService;
    private final DataContextService dataContextService;

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
            return "channel-input";
        }

        if (!dataContextService.passesDummyArtistValidation(channelUrl)) {
            model.addAttribute("showLoginModal", false);
            model.addAttribute("error", "해당 아티스트를 찾을 수 없습니다!");
            return "channel-input";
        }

        dataContextService.registerArtist(session, channelUrl);
        return "redirect:/artists";
    }
}
