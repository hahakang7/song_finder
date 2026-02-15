package hyun9.song_finder.controller;

import hyun9.song_finder.dto.DummyArtist;
import hyun9.song_finder.dto.DummyPlaylist;
import hyun9.song_finder.service.AuthStateService;
import hyun9.song_finder.service.DataContextService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import hyun9.song_finder.dto.CompareItemDTO;
import hyun9.song_finder.dto.CompareStatus;
import hyun9.song_finder.service.AuthStateService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import hyun9.song_finder.repository.ArtistSongRepository;
import hyun9.song_finder.repository.PlaylistSongRepository;
import hyun9.song_finder.repository.SubscribedArtistRepository;
import hyun9.song_finder.repository.SubscribedPlaylistRepository;
import hyun9.song_finder.service.DummyAuthService;
import hyun9.song_finder.service.DumpService;
import hyun9.song_finder.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    private final DataContextService dataContextService;

    @GetMapping({"/artist/{id}", "/artists/{id}"})
    public String artistPage(@PathVariable String id, Model model, HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);
        model.addAttribute("artist", dataContextService.findArtist(session, id).orElse(null));
        return "artist-detail";
    }

    @GetMapping("/compare/select-playlist")
    public String selectPlaylist(@RequestParam("artistId") String artistId,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "4") int size,
                                 Model model,
                                 HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);

        DummyArtist artist = dataContextService.findArtist(session, artistId).orElse(null);
        List<DummyPlaylist> subscribed = dataContextService.getSubscribedPlaylists(session);

        int totalPages = Math.max(1, (int) Math.ceil((double) subscribed.size() / size));
        int safePage = Math.max(1, Math.min(page, totalPages));
        int from = (safePage - 1) * size;
        int to = Math.min(from + size, subscribed.size());

        model.addAttribute("artist", artist);
        model.addAttribute("playlists", subscribed.subList(from, to));
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);

        return "compare-select-playlist";
    }

    @GetMapping({"/compare/result", "/compare"})
    public String compareResult(@RequestParam(value = "artistId", required = false) String artistId,
                                @RequestParam(value = "playlistId", required = false) String playlistId,
                                @RequestParam(value = "channelId", required = false) String channelId,
                                Model model,
                                HttpSession session) {
        boolean isAuthed = authStateService.isAuthed(session);
        model.addAttribute("isAuthed", isAuthed);
        model.addAttribute("showLoginModal", !isAuthed);

        String resolvedArtistId = artistId != null ? artistId : channelId;
        DummyArtist artist = dataContextService.findArtist(session, resolvedArtistId).orElse(null);
        DummyPlaylist playlist = dataContextService.findPlaylist(session, playlistId).orElse(null);

        model.addAttribute("artist", artist);
        model.addAttribute("playlist", playlist);
        model.addAttribute("missingTracks", dataContextService.getMissingTracks(resolvedArtistId, playlistId));

        return "compare-result";
    }
}
