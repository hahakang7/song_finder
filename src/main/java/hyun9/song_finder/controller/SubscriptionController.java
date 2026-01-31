package hyun9.song_finder.controller;

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
}

