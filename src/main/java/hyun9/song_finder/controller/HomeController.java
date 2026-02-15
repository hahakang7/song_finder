package hyun9.song_finder.controller;

import hyun9.song_finder.service.AuthStateService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AuthStateService authStateService;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);

        model.addAttribute("subscribedArtists", List.of(
                Map.of("artistName", "IU", "channelId", "artist-iu", "lastSyncedAt", "2026-02-10 09:30"),
                Map.of("artistName", "NewJeans", "channelId", "artist-newjeans", "lastSyncedAt", "2026-02-12 14:00")
        ));

        model.addAttribute("subscribedPlaylists", List.of(
                Map.of("playlistTitle", "내 최애곡", "playlistId", "pl-favorite", "lastSyncedAt", "2026-02-13 21:10"),
                Map.of("playlistTitle", "드라이브 노래", "playlistId", "pl-drive", "lastSyncedAt", "2026-02-09 18:20")
        ));

        return "home";
    }
}
