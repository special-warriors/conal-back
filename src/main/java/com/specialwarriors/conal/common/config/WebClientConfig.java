package com.specialwarriors.conal.common.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${github.token}")
    private String gitHubToken;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String clientSecret;

    @Bean
    public WebClient githubWebClient() {

        return WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.USER_AGENT, "spring-webclient")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + gitHubToken)
                .build();
    }

    @Bean
    public WebClient githubRevokeWebClient() {

        String basicAuth = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        return WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                .build();
    }

}
