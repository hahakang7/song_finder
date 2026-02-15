package hyun9.song_finder.controller;

import hyun9.song_finder.service.AuthStateService;
import hyun9.song_finder.service.DataContextService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/subscribe")
public class SubscriptionController {

    private final AuthStateService authStateService;
    private final DataContextService dataContextService;

    @PostMapping("/artist/toggle")
    public String toggleArtist(@RequestParam("artistId") String artistId, HttpSession session) {
        if (!authStateService.isAuthed(session)) {
            return "redirect:/artists?loginRequired=1";
        }
        dataContextService.unsubscribeArtist(session, artistId);
        return "redirect:/artists";
    }

    @PostMapping("/artist/resync")
    public String resyncArtist(@RequestParam("artistId") String artistId, HttpSession session) {
        if (!authStateService.isAuthed(session)) {
            return "redirect:/artists?loginRequired=1";
        }
        dataContextService.resyncArtist(session, artistId);
        return "redirect:/artists";
    }

    @PostMapping("/playlist/toggle")
    public String togglePlaylist(@RequestParam("playlistId") String playlistId, HttpSession session) {
        if (!authStateService.isAuthed(session)) {
            return "redirect:/playlists?loginRequired=1";
        }
        dataContextService.togglePlaylistSubscription(session, playlistId);
        return "redirect:/playlists";
    }

    @PostMapping("/playlist/resync")
    public String resyncPlaylist(@RequestParam("playlistId") String playlistId, HttpSession session) {
        if (!authStateService.isAuthed(session)) {
            return "redirect:/playlists?loginRequired=1";
        }
        dataContextService.resyncPlaylist(session, playlistId);
        return "redirect:/playlists";
    }
}
