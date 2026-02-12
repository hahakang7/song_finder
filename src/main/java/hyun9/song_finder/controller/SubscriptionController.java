package hyun9.song_finder.controller;

import hyun9.song_finder.repository.SubscribedArtistRepository;
import hyun9.song_finder.repository.SubscribedPlaylistRepository;
import hyun9.song_finder.service.SubscriptionSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
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
    private final OAuth2AuthorizedClientService clientService;

    @PostMapping("/artist")
    public String subscribeArtist(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("channelId") String channelId,
            @RequestParam("artistName") String artistName,
            @RequestParam("playlistId") String playlistId
    ) {

        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        subscriptionSyncService.subscribeAndSyncArtist(
                principal.getName(),
                accessToken,
                channelId,
                artistName
        );

        return "redirect:/compare?channelId="
                + channelId + "&playlistId=" + playlistId;
    }

    //아티스트 구독이 되어있으면 구독 해지하는 메서드
    @PostMapping("/artist/toggle")
    public String toggleArtist(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("channelId") String channelId,
            @RequestParam("artistName") String artistName
    ) {
        String userId = principal.getName();

        boolean subscribed =
                subscribedArtistRepository.existsByUserIdAndChannelId(userId, channelId);

        if (subscribed) {
            subscriptionSyncService.unsubscribeArtist(userId, channelId);
            return "redirect:/artist/" + channelId;
        }

        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

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
        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        subscriptionSyncService.subscribeAndSyncArtist(
                principal.getName(), accessToken, channelId, artistName
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

        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        subscriptionSyncService.subscribeAndSyncPlaylist(
                principal.getName(),
                accessToken,
                playlistId,
                playlistTitle
        );

        return "redirect:/compare?channelId="
                + channelId + "&playlistId=" + playlistId;
    }

    //플레이리스트 구독이 되어있으면 구독 해지하는 메서드
    @PostMapping("/playlist/toggle")
    public String togglePlaylist(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("playlistId") String playlistId,
            @RequestParam("playlistTitle") String playlistTitle
    ) {
        String userId = principal.getName();

        boolean subscribed =
                subscribedPlaylistRepository.existsByUserIdAndPlaylistId(userId, playlistId);

        if (subscribed) {
            subscriptionSyncService.unsubscribePlaylist(userId, playlistId);
            return "redirect:/playlists";
        }

        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        subscriptionSyncService.subscribeAndSyncPlaylist(
                userId, accessToken, playlistId, playlistTitle
        );

        return "redirect:/playlists";
    }



    //artist 재동기화
    @PostMapping("/resync/artist")
    public String resyncArtist(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("channelId") String channelId,
            @RequestParam("playlistId") String playlistId,
            @RequestParam("artistName") String artistName
    ) {

        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        subscriptionSyncService.subscribeAndSyncArtist(
                principal.getName(),
                accessToken,
                channelId,
                artistName
        );

        return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId;
    }

    //playlist 재동기화
    @PostMapping("/resync/playlist")
    public String resyncPlaylist(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("channelId") String channelId,
            @RequestParam("playlistId") String playlistId,
            @RequestParam("playlistTitle") String playlistTitle
    ) {

        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        subscriptionSyncService.subscribeAndSyncPlaylist(
                principal.getName(),
                accessToken,
                playlistId,
                playlistTitle
        );

        return "redirect:/compare?channelId=" + channelId + "&playlistId=" + playlistId;
    }


}



