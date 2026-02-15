package hyun9.song_finder.controller;

import hyun9.song_finder.repository.SubscribedArtistRepository;
import hyun9.song_finder.repository.SubscribedPlaylistRepository;
import hyun9.song_finder.service.DummyAuthService;
import hyun9.song_finder.service.SubscriptionSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/subscribe")
public class SubscriptionController {

    private final SubscriptionSyncService subscriptionSyncService;
    private final SubscribedPlaylistRepository subscribedPlaylistRepository;
    private final SubscribedArtistRepository subscribedArtistRepository;
    private final DummyAuthService dummyAuthService;

    @PostMapping("/artist")
    public String subscribeArtist(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("channelId") String channelId,
            @RequestParam("artistName") String artistName,
            @RequestParam("playlistId") String playlistId
    ) {

        String userId = dummyAuthService.resolveUserId(principal);
        String accessToken = dummyAuthService.resolveAccessToken(principal);

        subscriptionSyncService.subscribeAndSyncArtist(
                userId,
                accessToken,
                channelId,
                artistName
        );

        return "redirect:/compare?channelId="
                + channelId + "&playlistId=" + playlistId;
    }

    @PostMapping("/artist/toggle")
    public String toggleArtist(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("channelId") String channelId,
            @RequestParam("artistName") String artistName
    ) {
        String userId = dummyAuthService.resolveUserId(principal);

        boolean subscribed =
                subscribedArtistRepository.existsByUserIdAndChannelId(userId, channelId);

        if (subscribed) {
            subscriptionSyncService.unsubscribeArtist(userId, channelId);
            return "redirect:/artist/" + channelId;
        }

        String accessToken = dummyAuthService.resolveAccessToken(principal);

        subscriptionSyncService.subscribeAndSyncArtist(
                userId, accessToken, channelId, artistName
        );

        return "redirect:/artist/" + channelId;
    }

    @PostMapping("/artist/resync")
    public String resyncArtist(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("channelId") String channelId,
            @RequestParam("artistName") String artistName
    ) {
        String userId = dummyAuthService.resolveUserId(principal);
        String accessToken = dummyAuthService.resolveAccessToken(principal);

        subscriptionSyncService.subscribeAndSyncArtist(
                userId, accessToken, channelId, artistName
        );

        return "redirect:/artist/" + channelId;
    }



    @PostMapping("/playlist")
    public String subscribePlaylist(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("playlistId") String playlistId,
            @RequestParam("playlistTitle") String playlistTitle,
            @RequestParam("channelId") String channelId
    ) {

        String userId = dummyAuthService.resolveUserId(principal);
        String accessToken = dummyAuthService.resolveAccessToken(principal);

        subscriptionSyncService.subscribeAndSyncPlaylist(
                userId,
                accessToken,
                playlistId,
                playlistTitle
        );

        return "redirect:/compare?channelId="
                + channelId + "&playlistId=" + playlistId;
    }

    @PostMapping("/playlist/toggle")
    public String togglePlaylist(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("playlistId") String playlistId,
            @RequestParam("playlistTitle") String playlistTitle
    ) {
        String userId = dummyAuthService.resolveUserId(principal);

        boolean subscribed =
                subscribedPlaylistRepository.existsByUserIdAndPlaylistId(userId, playlistId);

        if (subscribed) {
            subscriptionSyncService.unsubscribePlaylist(userId, playlistId);
            return "redirect:/playlists";
        }

        String accessToken = dummyAuthService.resolveAccessToken(principal);

        subscriptionSyncService.subscribeAndSyncPlaylist(
                userId, accessToken, playlistId, playlistTitle
        );

        return "redirect:/playlists";
    }



    @PostMapping("/resync/artist")
    public String resyncArtist(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("channelId") String channelId,
            @RequestParam("playlistId") String playlistId,
            @RequestParam("artistName") String artistName
    ) {

        String userId = dummyAuthService.resolveUserId(principal);
        String accessToken = dummyAuthService.resolveAccessToken(principal);

        subscriptionSyncService.subscribeAndSyncArtist(
                userId,
                accessToken,
                channelId,
                artistName
        );

        return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId;
    }

    @PostMapping("/resync/playlist")
    public String resyncPlaylist(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("channelId") String channelId,
            @RequestParam("playlistId") String playlistId,
            @RequestParam("playlistTitle") String playlistTitle
    ) {

        String userId = dummyAuthService.resolveUserId(principal);
        String accessToken = dummyAuthService.resolveAccessToken(principal);

        subscriptionSyncService.subscribeAndSyncPlaylist(
                userId,
                accessToken,
                playlistId,
                playlistTitle
        );

        return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId;
    }


}
