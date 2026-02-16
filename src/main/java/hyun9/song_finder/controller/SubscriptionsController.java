package hyun9.song_finder.controller;

import hyun9.song_finder.domain.SubscribedArtist;
import hyun9.song_finder.domain.SubscribedPlaylist;
import hyun9.song_finder.repository.SubscribedArtistRepository;
import hyun9.song_finder.repository.SubscribedPlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SubscriptionsController {

    private final SubscribedArtistRepository subscribedArtistRepository;
    private final SubscribedPlaylistRepository subscribedPlaylistRepository;

    @GetMapping("/artists/subscribed")
    public String subscribedArtists(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Model model
    ) {

        if (principal == null) return "redirect:/oauth2/authorization/google";

        // 방어: 음수/과대값 보정
        page = Math.max(page, 0);
        size = Math.min(Math.max(size, 1), 100);

        String userId = principal.getName();

        // 정렬 기준이 lastSyncedAt이면 엔티티 필드명과 일치해야 함
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "lastSyncedAt")
        );

        Page<SubscribedArtist> result = subscribedArtistRepository.findByUserId(userId, pageable);

        List<SubscribedArtist> artists = subscribedArtistRepository.findByUserId(userId);
        List<SubscribedPlaylist> playlists = subscribedPlaylistRepository.findByUserId(userId);

        model.addAttribute("subscribedArtists", artists);
        model.addAttribute("subscribedPlaylists", playlists);

        model.addAttribute("subscribedArtists", result.getContent());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("hasPrev", result.hasPrevious());
        model.addAttribute("hasNext", result.hasNext());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());

        return "artists-subscribed";
    }
}
