package com.specialwarriors.conal.common.auth.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SessionManager {

    public void createSession(HttpServletRequest request, Long userId) {
        HttpSession session = request.getSession();
        session.setAttribute("userId", userId);
    }

    public Long getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        return Optional.ofNullable(session)
                .map(s -> s.getAttribute("userId"))
                .map(Long.class::cast)
                .orElse(null);
    }

    public void clearSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
