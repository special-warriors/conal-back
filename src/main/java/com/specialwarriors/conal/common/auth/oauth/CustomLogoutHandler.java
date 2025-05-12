package com.specialwarriors.conal.common.auth.oauth;

import com.specialwarriors.conal.common.auth.session.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final SessionManager sessionManager;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {

        sessionManager.clearSession(request);
    }
}
