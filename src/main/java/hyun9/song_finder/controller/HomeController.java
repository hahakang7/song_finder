package hyun9.song_finder.controller;

import hyun9.song_finder.service.AuthStateService;
import hyun9.song_finder.service.DataContextService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AuthStateService authStateService;
    private final DataContextService dataContextService;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);

        model.addAttribute("subscribedArtists", dataContextService.getSubscribedArtists(session));
        model.addAttribute("subscribedPlaylists", dataContextService.getSubscribedPlaylists(session));

        return "home";
    }
}
