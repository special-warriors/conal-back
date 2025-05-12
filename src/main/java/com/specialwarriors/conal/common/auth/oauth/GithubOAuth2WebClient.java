package com.specialwarriors.conal.common.auth.oauth;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class GithubOAuth2WebClient {

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientId;

    private final WebClient githubRevokeWebClient;

    public void unlink(String oauthAccessToken) {

        if (oauthAccessToken == null || oauthAccessToken.isBlank()) {
            throw new IllegalArgumentException("access_token이 null이거나 비어 있습니다.");
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
