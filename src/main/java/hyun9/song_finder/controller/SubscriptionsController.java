package hyun9.song_finder.controller;

import hyun9.song_finder.domain.SubscribedArtist;
import hyun9.song_finder.domain.SubscribedPlaylist;
import hyun9.song_finder.repository.SubscribedArtistRepository;
import hyun9.song_finder.repository.SubscribedPlaylistRepository;
import hyun9.song_finder.service.DummyAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SubscriptionsController {

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
