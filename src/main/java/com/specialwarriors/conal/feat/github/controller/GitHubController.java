package com.specialwarriors.conal.feat.github.controller;

import com.specialwarriors.conal.feat.github.service.GitHubService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService githubService;


    @GetMapping("/user/{username}")
    public Flux<Map<String, Object>> getUser(@PathVariable String username) {
        return githubService.getUserInfo(username);
    }

    @GetMapping("users/{username}/repos")
    public Flux<Map<String, Object>> getRepos(@PathVariable String username) {
        return githubService.getRepos(username);
    }

    @GetMapping("repos/{username}/{repo}")
    public Flux<Map<String, Object>> getRepo(@PathVariable String username,
        @PathVariable String repo) {
        return githubService.getRepo(username, repo);
    }

    @GetMapping("/repos/{username}/{repo}/commits")
    public Flux<Map<String, Object>> getCommits(@PathVariable String username,
        @PathVariable String repo) {
        return githubService.getCommit(username, repo);
    }

}