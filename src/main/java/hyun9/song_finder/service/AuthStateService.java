package hyun9.song_finder.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class AuthStateService {

    private static final String AUTH_KEY = "isAuthed";

    public boolean isAuthed(HttpSession session) {
        Object value = session.getAttribute(AUTH_KEY);
        return value instanceof Boolean && (Boolean) value;
    }

    public void login(HttpSession session) {
        session.setAttribute(AUTH_KEY, true);
    }

    public void logout(HttpSession session) {
        session.setAttribute(AUTH_KEY, false);
    }
}
