package hyun9.song_finder.controller;

import hyun9.song_finder.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Slf4j
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
                                    RedirectAttributes ra,
                                    Model model) {

        String channelId = youtubeService.extractChannelId(channelUrl);

        Map<String, Object> channel = youtubeService.fetchChannelInfo(channelId);

        if (channel == null) {
            model.addAttribute("error", "채널 정보를 가져오지 못했습니다. 채널 ID가 맞는지 확인하세요.");
            return "redirect:/artist/link";
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

        String artistName = null;
        if (channel != null) {
            Map<String, Object> snippet = (Map<String, Object>) channel.get("snippet");
            if (snippet != null) {
                artistName = (String) snippet.get("title");
            }
        }
        model.addAttribute("artistName", artistName);

        String artistThumbnailUrl = null;
        if (channel != null) {
            Map<String, Object> snippet = (Map<String, Object>) channel.get("snippet");
            if (snippet != null) {
                Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                if (thumbnails != null) {
                    // default가 없을 수도 있으니 high/medium도 fallback
                    Map<String, Object> thumb =
                            (Map<String, Object>) thumbnails.getOrDefault("default",
                                    thumbnails.getOrDefault("medium", thumbnails.get("high")));

                    if (thumb != null) {
                        artistThumbnailUrl = (String) thumb.get("url");
                    }
                }
            }
        }
        model.addAttribute("artistThumbnailUrl", artistThumbnailUrl);




        model.addAttribute("channelId", channelId);
        model.addAttribute("channelInfo", channel);
        model.addAttribute("playlists", playlists);

        return "redirect:/artist/registered?channelId=" + channelId;
    }

    @GetMapping("/artist/registered")
    public String showChannelRegistered(@RequestParam("channelId") String channelId,
                                        @AuthenticationPrincipal OAuth2User principal,
                                        Model model) {

        Map<String, Object> channel = youtubeService.fetchChannelInfo(channelId);
        if (channel == null) {
            model.addAttribute("error", "채널 정보를 가져오지 못했습니다.");
            return "channel-input";
        }

        // 로그인 사용자 access token
        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient("google", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        Map<String, Object> playlistResult =
                youtubeService.getPaginatedPlaylists(accessToken, null);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> playlists =
                (List<Map<String, Object>>) playlistResult.get("playlists");

        // artistName
        String artistName = null;
        Map<String, Object> snippet = (Map<String, Object>) channel.get("snippet");
        if (snippet != null) {
            artistName = (String) snippet.get("title");
        }
        model.addAttribute("artistName", artistName);

        // thumbnail
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

        log.info("artistThumbnailUrl = " + artistThumbnailUrl);

        model.addAttribute("artistThumbnailUrl", artistThumbnailUrl);

        model.addAttribute("channelId", channelId);
        model.addAttribute("channelInfo", channel);
        model.addAttribute("playlists", playlists);

        return "channel-registered";
    }

}
