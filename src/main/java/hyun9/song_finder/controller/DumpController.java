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
    public String dump(@RequestParam("channelId") String channelId,
                       @RequestParam("playlistId") String playlistId,
                       HttpSession session) {
        if (!authStateService.isAuthed(session)) {
            return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId + "&loginRequired=1";
        }
        return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId;
    }

    @PostMapping("/undo")
    public String undo(@RequestParam("channelId") String channelId,
                       @RequestParam("playlistId") String playlistId,
                       HttpSession session) {
        if (!authStateService.isAuthed(session)) {
            return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId + "&loginRequired=1";
        }
        return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId;
    }
}
