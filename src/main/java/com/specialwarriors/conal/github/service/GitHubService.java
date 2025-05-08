package com.specialwarriors.conal.github.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class GitHubService {

    private final WebClient githubWebClient;

    public Flux<Map<String, Object>> getUserInfo(String username) {
        return githubWebClient.get()
                .uri("/users/{username}", username)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<>() {
                });
    }

    public Flux<Map<String, Object>> getRepos(String username) {
        return githubWebClient.get()
                .uri("users/{username}/repos", username)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<>() {
                });
    }

    public Flux<Map<String, Object>> getRepo(String username, String repo) {
        return githubWebClient.get()
                .uri("repos/{username}/{repo}", username, repo)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<>() {
                });
    }

    public Flux<Map<String, Object>> getCommit(String username, String repo) {
        return githubWebClient.get()
                .uri("repos/{username}/{repo}/commits", username, repo)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<>() {
                });
    }

}
