package hyun9.song_finder.controller;

import hyun9.song_finder.service.AuthStateService;
import jakarta.servlet.http.HttpSession;
import hyun9.song_finder.domain.SubscribedArtist;
import hyun9.song_finder.domain.SubscribedPlaylist;
import hyun9.song_finder.repository.SubscribedArtistRepository;
import hyun9.song_finder.repository.SubscribedPlaylistRepository;
import hyun9.song_finder.service.DummyAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SubscriptionsController {

    private final AuthStateService authStateService;

    @GetMapping("/subscriptions")
    public String subscriptions(Model model, HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);

        model.addAttribute("subscribedArtists", List.of(
                Map.of("artistName", "IU", "channelId", "artist-iu", "lastSyncedAt", "2026-02-10 09:30"),
                Map.of("artistName", "NewJeans", "channelId", "artist-newjeans", "lastSyncedAt", "2026-02-12 14:00")
        ));
        model.addAttribute("subscribedPlaylists", List.of(
                Map.of("playlistTitle", "내 최애곡", "playlistId", "pl-favorite", "lastSyncedAt", "2026-02-13 21:10"),
                Map.of("playlistTitle", "드라이브 노래", "playlistId", "pl-drive", "lastSyncedAt", "2026-02-09 18:20")
        ));
    private final SubscribedArtistRepository subscribedArtistRepository;
    private final SubscribedPlaylistRepository subscribedPlaylistRepository;
    private final DummyAuthService dummyAuthService;

    @GetMapping("/subscriptions")
    public String subscriptions(@AuthenticationPrincipal OAuth2User principal, Model model) {

        String userId = dummyAuthService.resolveUserId(principal);

        List<SubscribedArtist> artists = subscribedArtistRepository.findByUserId(userId);
        List<SubscribedPlaylist> playlists = subscribedPlaylistRepository.findByUserId(userId);

        model.addAttribute("subscribedArtists", artists);
        model.addAttribute("subscribedPlaylists", playlists);

        return "subscriptions";
    }
}
