package hyun9.song_finder.controller;

import hyun9.song_finder.service.AuthStateService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dump")
public class DumpController {

    private final AuthStateService authStateService;

    @PostMapping("/add")
    public String dump(@RequestParam("artistId") String artistId,
                       @RequestParam("playlistId") String playlistId,
                       HttpSession session) {
        if (!authStateService.isAuthed(session)) {
            return "redirect:/compare/result?artistId=" + artistId + "&playlistId=" + playlistId + "&loginRequired=1";
        }
        return "redirect:/compare/result?artistId=" + artistId + "&playlistId=" + playlistId;
    }
}
