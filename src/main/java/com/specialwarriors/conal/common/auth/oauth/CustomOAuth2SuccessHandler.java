package com.specialwarriors.conal.common.auth.oauth;

import com.specialwarriors.conal.common.auth.session.SessionManager;
import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.exception.UserException;
import com.specialwarriors.conal.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        long githubId = Long.parseLong(oauth2User.getAttribute("id").toString());

        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND));
        sessionManager.createSession(request, user.getId());

        response.sendRedirect("/login/success");
    }
}
