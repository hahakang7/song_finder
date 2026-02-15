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
@RequestMapping("/subscribe")
public class SubscriptionController {

    private final AuthStateService authStateService;

    @PostMapping("/artist")
    public String subscribeArtist(@RequestParam("channelId") String channelId,
                                  @RequestParam("playlistId") String playlistId,
                                  HttpSession session) {
        if (!authStateService.isAuthed(session)) {
            return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId + "&loginRequired=1";
        }
        return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId;
    }

    @PostMapping("/artist/toggle")
    public String toggleArtist(@RequestParam("channelId") String channelId,
                               HttpSession session) {
        if (!authStateService.isAuthed(session)) {
            return "redirect:/artist/" + channelId + "?loginRequired=1";
        }
        return "redirect:/artist/" + channelId;
    }

    @PostMapping("/artist/resync")
    public String resyncArtist(@RequestParam("channelId") String channelId,
                               HttpSession session) {
        if (!authStateService.isAuthed(session)) {
            return "redirect:/artist/" + channelId + "?loginRequired=1";
        }
        return "redirect:/artist/" + channelId;
    }

    @PostMapping("/playlist")
    public String subscribePlaylist(@RequestParam("playlistId") String playlistId,
                                    @RequestParam("channelId") String channelId,
                                    HttpSession session) {
        if (!authStateService.isAuthed(session)) {
            return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId + "&loginRequired=1";
        }
        return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId;
    }

    @PostMapping("/playlist/toggle")
    public String togglePlaylist(HttpSession session) {
        if (!authStateService.isAuthed(session)) {
            return "redirect:/playlists?loginRequired=1";
        }
        return "redirect:/playlists";
    }

    @PostMapping("/resync/artist")
    public String resyncArtistFromCompare(@RequestParam("channelId") String channelId,
                                          @RequestParam("playlistId") String playlistId,
                                          HttpSession session) {
        if (!authStateService.isAuthed(session)) {
            return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId + "&loginRequired=1";
        }
        return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId;
    }

    @PostMapping("/resync/playlist")
    public String resyncPlaylistFromCompare(@RequestParam("channelId") String channelId,
                                            @RequestParam("playlistId") String playlistId,
                                            HttpSession session) {
        if (!authStateService.isAuthed(session)) {
            return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId + "&loginRequired=1";
        }
        return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId;
    }
}
