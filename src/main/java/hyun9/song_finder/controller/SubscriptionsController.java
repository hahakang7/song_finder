package hyun9.song_finder.controller;

import hyun9.song_finder.dto.DummyArtist;
import hyun9.song_finder.service.AuthStateService;
import hyun9.song_finder.service.DataContextService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SubscriptionsController {

    private final AuthStateService authStateService;
    private final DataContextService dataContextService;

    @GetMapping({"/subscriptions", "/artists"})
    public String subscriptions(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "4") int size,
                                Model model,
                                HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);

        List<DummyArtist> allArtists = dataContextService.getSubscribedArtists(session);
        int totalPages = Math.max(1, (int) Math.ceil((double) allArtists.size() / size));
        int safePage = Math.max(1, Math.min(page, totalPages));
        int from = (safePage - 1) * size;
        int to = Math.min(from + size, allArtists.size());

        model.addAttribute("artists", allArtists.subList(from, to));
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);

        return "subscriptions";
    }
}
