package hyun9.song_finder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DummyAuthService {

    private static final String REGISTRATION_ID = "google";

    private final OAuth2AuthorizedClientService clientService;

    @Value("${app.auth.dummy-user-id:demo-user}")
    private String dummyUserId;

    @Value("${app.auth.dummy-access-token:}")
    private String dummyAccessToken;

    public String resolveUserId(OAuth2User principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return dummyUserId;
        }
        return principal.getName();
    }

    public String resolveAccessToken(OAuth2User principal) {
        if (principal == null) {
            return dummyAccessToken;
        }

        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient(REGISTRATION_ID, principal.getName());

        if (client == null || client.getAccessToken() == null) {
            return dummyAccessToken;
        }

        return client.getAccessToken().getTokenValue();
    }
}
