package hyun9.song_finder.controller;

import hyun9.song_finder.dto.DummyPlaylist;
import hyun9.song_finder.service.AuthStateService;
import hyun9.song_finder.service.DataContextService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PlaylistController {

    private final AuthStateService authStateService;
    private final DataContextService dataContextService;

    @GetMapping("/playlists")
    public String getUserPlaylists(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "4") int size,
                                   @RequestParam(required = false) String detailId,
                                   Model model,
                                   HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);

        List<DummyPlaylist> all = dataContextService.getPlaylists(session);
        int totalPages = Math.max(1, (int) Math.ceil((double) all.size() / size));
        int safePage = Math.max(1, Math.min(page, totalPages));
        int from = (safePage - 1) * size;
        int to = Math.min(from + size, all.size());

        model.addAttribute("playlists", all.subList(from, to));
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("detailId", detailId);

        return "playlists";
    }

    @GetMapping("/playlist/{id}")
    public String getPlaylistItems(@PathVariable String id, Model model, HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);
        model.addAttribute("playlist", dataContextService.findPlaylist(session, id).orElse(null));
        return "playlist-detail";
    }
}
