package hyun9.song_finder.controller;

import hyun9.song_finder.service.DumpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dump")
public class DumpController {

    private final DumpService dumpService;

    @PostMapping("/add")
    public String dump(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("channelId") String channelId,
            @RequestParam("playlistId") String playlistId,
            @RequestParam("title") String normalizedTitle
    ) {
        String userId = principal.getName();

        dumpService.dump(userId, channelId, normalizedTitle);

        return "redirect:/compare?channelId="
                + channelId + "&playlistId=" + playlistId;
    }

    @PostMapping("/undo")
    public String undo(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("channelId") String channelId,
            @RequestParam("playlistId") String playlistId,
            @RequestParam("title") String normalizedTitle
    ) {
        String userId = principal.getName();

        dumpService.undo(userId, channelId, normalizedTitle);

        return "redirect:/compare?channelId="
                + channelId + "&playlistId=" + playlistId;
    }
}

