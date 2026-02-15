package hyun9.song_finder.controller;

import hyun9.song_finder.service.AuthStateService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthStateService authStateService;

    @PostMapping("/auth/google-login")
    public String googleLogin(@RequestParam(defaultValue = "/") String redirect,
                              HttpSession session) {
        authStateService.login(session);
        return "redirect:" + redirect;
    }

    @PostMapping("/auth/logout")
    public String logout(@RequestParam(defaultValue = "/") String redirect,
                         HttpSession session) {
        authStateService.logout(session);
        return "redirect:" + redirect;
    }
}
