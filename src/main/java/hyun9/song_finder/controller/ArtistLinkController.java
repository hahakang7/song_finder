package hyun9.song_finder.controller;

import hyun9.song_finder.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ArtistLinkController {

    private final YoutubeService youtubeService;
    private final OAuth2AuthorizedClientService clientService;

    // 채널 URL 입력 페이지
    @GetMapping("/artist/link")
    public String showChannelInput() {
        return "channel-input";
    }

    // 채널 URL 제출 → 채널 정보 확인 + 플레이리스트 목록 보여주기
    @PostMapping("/artist/link")
    public String handleChannelLink(@RequestParam("channelUrl") String channelUrl,
                                    @AuthenticationPrincipal OAuth2User principal,
                                    Model model) {

        String channelId = youtubeService.extractChannelId(channelUrl);
        if (channelId == null) {
            model.addAttribute("error", "채널 URL에서 채널 ID를 추출하지 못했습니다.");
            return "channel-input";
        }

        Map<String, Object> channel = youtubeService.fetchChannelInfo(channelId);
        if (channel == null) {
            model.addAttribute("error", "채널 정보를 가져오지 못했습니다. 채널 ID가 맞는지 확인하세요.");
            return "channel-input";
        }

        // 로그인 사용자 access token
        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        // 내 플레이리스트 1페이지(5개)만 우선 보여주기 (필요하면 페이지네이션으로 확장)
        Map<String, Object> playlistResult =
                youtubeService.getPaginatedPlaylists(accessToken, null);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> playlists =
                (List<Map<String, Object>>) playlistResult.get("playlists");

        model.addAttribute("channelId", channelId);
        model.addAttribute("channel", channel);
        model.addAttribute("playlists", playlists);

        return "channel-registered";
    }
}
