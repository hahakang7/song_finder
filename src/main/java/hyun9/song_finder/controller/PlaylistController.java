package hyun9.song_finder.controller;

import hyun9.song_finder.service.AuthStateService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PlaylistController {

    private final AuthStateService authStateService;

    @GetMapping("/playlists")
    public String getUserPlaylists(@RequestParam(required = false) String channelId,
                                   Model model,
                                   HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);
        model.addAttribute("selectedChannelId", channelId);

        model.addAttribute("playlists", List.of(
                Map.of("id", "pl-favorite", "title", "내 최애곡", "subscribed", true, "lastSyncedAt", "2026-02-13 21:10"),
                Map.of("id", "pl-drive", "title", "드라이브 노래", "subscribed", true, "lastSyncedAt", "2026-02-09 18:20"),
                Map.of("id", "pl-study", "title", "집중용 플레이리스트", "subscribed", false, "lastSyncedAt", "-")
        ));

        return "playlists";
    }

    @GetMapping("/playlist/{id}")
    public String getPlaylistItems(@PathVariable String id, Model model, HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);
        model.addAttribute("id", id);

        model.addAttribute("playlistItems", List.of(
                Map.of("title", "Ditto"),
                Map.of("title", "밤편지"),
                Map.of("title", "Attention")
        ));

        return "playlist-detail";
    }
}
