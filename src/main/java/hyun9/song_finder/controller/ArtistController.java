package hyun9.song_finder.controller;

import hyun9.song_finder.dto.CompareItemDTO;
import hyun9.song_finder.dto.CompareStatus;
import hyun9.song_finder.service.AuthStateService;
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
public class ArtistController {

    private final AuthStateService authStateService;

    @GetMapping("/compare")
    public String compare(@RequestParam("channelId") String channelId,
                          @RequestParam("playlistId") String playlistId,
                          Model model,
                          HttpSession session) {

        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);

        List<CompareItemDTO> items = List.of(
                new CompareItemDTO("Ditto", "https://placehold.co/48x48", CompareStatus.CONTAINED),
                new CompareItemDTO("Hype Boy", "https://placehold.co/48x48", CompareStatus.MISSING),
                new CompareItemDTO("밤편지", "https://placehold.co/48x48", CompareStatus.DUMPED)
        );

        model.addAttribute("items", items);
        model.addAttribute("compareMode", "DUMMY");
        model.addAttribute("isFastMode", true);
        model.addAttribute("channelId", channelId);
        model.addAttribute("playlistId", playlistId);
        model.addAttribute("artistName", "Dummy Artist");
        model.addAttribute("playlistTitle", "내 최애곡");
        model.addAttribute("artistSubscribed", true);
        model.addAttribute("playlistSubscribed", true);
        model.addAttribute("dumpedCount", 1);
        model.addAttribute("missingCount", 1);
        model.addAttribute("containedCount", 1);

        return "compare-result";
    }

    @GetMapping("/artist/{channelId}")
    public String artistPage(@PathVariable String channelId,
                             Model model,
                             HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);

        model.addAttribute("channelId", channelId);
        model.addAttribute("artistName", "Dummy Artist");
        model.addAttribute("artistThumbnailUrl", "https://placehold.co/88x88");
        model.addAttribute("artistSubscribed", true);
        model.addAttribute("artistLastSyncedAt", "2026-02-12 14:00");

        return "artist-detail";
    }
}
