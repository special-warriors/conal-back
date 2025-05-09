package com.specialwarriors.conal.common.auth.oauth;

import com.specialwarriors.conal.common.auth.exception.AuthException;
import com.specialwarriors.conal.common.auth.exception.CustomAuthException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Slf4j
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        if (exception.getMessage().contains("redirect")) {
            throw new CustomAuthException(AuthException.INVALID_REDIRECT_URI);
        } else if (exception.getMessage().contains("user cancelled")) {
            throw new CustomAuthException(AuthException.OAUTH_USER_CANCELLED);
        } else {
            throw new CustomAuthException(AuthException.OAUTH_PROVIDER_ERROR);
        }
    }
}
