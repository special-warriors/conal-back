package com.specialwarriors.conal.common.auth.oauth;

import com.specialwarriors.conal.common.auth.session.SessionManager;
import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.exception.UserException;
import com.specialwarriors.conal.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        int githubId = oauth2User.getAttribute("id");

        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND));
        sessionManager.createSession(request, user.getId());

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                registrationId,
                oauthToken.getName()
        );

        String accessToken = client.getAccessToken().getTokenValue();
        String redisKey = "github:token:" + githubId;
        redisTemplate.opsForValue().set(redisKey, accessToken, Duration.ofHours(1));

        response.sendRedirect("/login/success");
    }
}
