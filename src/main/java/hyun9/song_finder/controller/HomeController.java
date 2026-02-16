package hyun9.song_finder.controller;

import hyun9.song_finder.domain.SubscribedArtist;
import hyun9.song_finder.domain.SubscribedPlaylist;
import hyun9.song_finder.repository.SubscribedArtistRepository;
import hyun9.song_finder.repository.SubscribedPlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SubscribedArtistRepository subscribedArtistRepository;
    private final SubscribedPlaylistRepository subscribedPlaylistRepository;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {

        boolean authed = (principal != null);
        model.addAttribute("isAuthenticated", authed);

        if (!authed) {
            model.addAttribute("subscribedArtists", List.of());
            model.addAttribute("subscribedPlaylists", List.of());
            // 로그인 버튼은 sec:authorize가 처리하므로 여기서 추가 작업 없음
            return "home";
        }

        // 로그인 사용자 정보 (Google OAuth2의 표준 클레임)
        model.addAttribute("userName", principal.getAttribute("name"));
        model.addAttribute("userAvatarUrl", principal.getAttribute("picture"));

        String userId = principal.getName();

        List<SubscribedArtist> artists = subscribedArtistRepository.findByUserId(userId);
        List<SubscribedPlaylist> playlists = subscribedPlaylistRepository.findByUserId(userId);

        model.addAttribute("subscribedArtists", artists);
        model.addAttribute("subscribedPlaylists", playlists);

        return "home";
    }
}
