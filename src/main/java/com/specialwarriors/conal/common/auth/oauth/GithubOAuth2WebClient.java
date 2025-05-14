package com.specialwarriors.conal.common.auth.oauth;

import com.specialwarriors.conal.common.auth.exception.AuthException;
import com.specialwarriors.conal.common.exception.GeneralException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class GithubOAuth2WebClient {

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientId;

    private final WebClient githubRevokeWebClient;

    public void unlink(String oauthAccessToken) {

        if (!StringUtils.hasText(oauthAccessToken)) {
            throw new GeneralException(AuthException.EMPTY_OAUTH_TOKEN);
        }

        Map<String, String> body = Map.of("access_token", oauthAccessToken);

        githubRevokeWebClient
                .method(HttpMethod.DELETE)
                .uri("/applications/{clientId}/grant", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
