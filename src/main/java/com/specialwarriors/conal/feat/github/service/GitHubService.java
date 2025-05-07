package com.specialwarriors.conal.feat.github.service;

import java.util.List;
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

    public Flux<Map<String, Object>> getUserInfo(String owner) {
        return githubWebClient.get()
            .uri("/users/{owner}", owner)
            .retrieve()
            .bodyToFlux(new ParameterizedTypeReference<>() {
            });
    }

    public Flux<Map<String, Object>> getRepos(String owner) {
        return githubWebClient.get()
            .uri("users/{owner}/repos", owner)
            .retrieve()
            .bodyToFlux(new ParameterizedTypeReference<>() {
            });
    }

    public Flux<Map<String, Object>> getRepo(String owner, String repo) {
        return githubWebClient.get()
            .uri("repos/{owner}/{repo}", owner, repo)
            .retrieve()
            .bodyToFlux(new ParameterizedTypeReference<>() {
            });
    }

    public Flux<Map<String, Object>> getCommit(String owner, String repo) {
        return githubWebClient.get()
            .uri("repos/{username}/{repo}/commits", owner, repo)
            .retrieve()
            .bodyToFlux(new ParameterizedTypeReference<>() {
            });
    }

    public Flux<Map<String, Object>> getContributors(String owner, String repo) {
        return githubWebClient.get()
            .uri(
                "/repos/{owner}/{repo}/contributors",
                owner, repo)
            .retrieve()
            .bodyToFlux(new ParameterizedTypeReference<>() {
            });
    }

    public Flux<Map<String, Object>> getCommits(String username, String owner, String repo) {
        return githubWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/repos/{owner}/{repo}/commits")
                .queryParam("author", username)
                .build(owner, repo))
            .retrieve()
            .bodyToFlux(new ParameterizedTypeReference<>() {
            });
    }

    public Flux<Map<String, Object>> getAllPullRequest(String owner, String repo) {
        return githubWebClient.get()
            .uri(
                "/repos/{owner}/{repo}/pulls?state=closed",
                owner, repo)
            .retrieve()
            .bodyToFlux(new ParameterizedTypeReference<>() {
            });
    }


    public Flux<Map<String, Object>> getPullRequests(String username, String owner,
        String repo) {

        String q = "repo:" + owner + "/" + repo + "+type:pr+author:" + username;

        return githubWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/search/issues")
                .queryParam("q", q)
                .build())
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
            })
            .flatMapMany(response -> {
                Object items = response.get("items");
                if (items instanceof List<?> list) {
                    return Flux.fromIterable((List<Map<String, Object>>) list);
                } else {
                    return Flux.empty();
                }
            });
    }

}
