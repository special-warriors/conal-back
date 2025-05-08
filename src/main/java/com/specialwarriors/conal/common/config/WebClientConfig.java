package com.specialwarriors.conal.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${github.token}")
    private String gitHubToken;

    @Bean
    public WebClient githubWebClient() {
        return WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader(HttpHeaders.USER_AGENT, "spring-webclient")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + gitHubToken)
            .build();
    }

}
