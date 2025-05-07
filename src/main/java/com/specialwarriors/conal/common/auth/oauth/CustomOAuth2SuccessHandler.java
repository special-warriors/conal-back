package com.specialwarriors.conal.common.auth.oauth;

import com.specialwarriors.conal.common.auth.jwt.JwtProvider;
import com.specialwarriors.conal.common.auth.jwt.JwtTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Long userId = Long.valueOf(oauth2User.getAttribute("id").toString());

        JwtTokenResponse tokens = jwtProvider.generateTokens(userId);

        response.setHeader("Authorization", "Bearer " + tokens.accessToken());
        response.setHeader("Refresh-Token", tokens.refreshToken());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
//        response.getWriter().write("""
//                {
//                  "accessToken": "%s",
//                  "refreshToken": "%s"
//                }
//                """.formatted(tokens.accessToken(), tokens.refreshToken())
//        );

        response.sendRedirect("/home");
    }
}
