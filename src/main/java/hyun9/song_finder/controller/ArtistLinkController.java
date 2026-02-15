package hyun9.song_finder.controller;

import hyun9.song_finder.service.DummyAuthService;
import hyun9.song_finder.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final DummyAuthService dummyAuthService;

    @GetMapping("/artist/link")
    public String showChannelInput() {
        return "channel-input";
    }

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

        String accessToken = dummyAuthService.resolveAccessToken(principal);

        Map<String, Object> playlistResult =
                youtubeService.getPaginatedPlaylists(accessToken, null);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> playlists =
                (List<Map<String, Object>>) playlistResult.get("playlists");

        String artistName = null;
        Map<String, Object> snippet = (Map<String, Object>) channel.get("snippet");
        if (snippet != null) {
            artistName = (String) snippet.get("title");
        }
        model.addAttribute("artistName", artistName);

        String artistThumbnailUrl = null;
        if (snippet != null) {
            Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
            if (thumbnails != null) {
                Map<String, Object> thumb =
                        (Map<String, Object>) thumbnails.getOrDefault("default",
                                thumbnails.getOrDefault("medium", thumbnails.get("high")));

                if (thumb != null) {
                    artistThumbnailUrl = (String) thumb.get("url");
                }
            }
        }
        model.addAttribute("artistThumbnailUrl", artistThumbnailUrl);

        model.addAttribute("channelId", channelId);
        model.addAttribute("channelInfo", channel);
        model.addAttribute("playlists", playlists);

        return "channel-registered";
    }
}
