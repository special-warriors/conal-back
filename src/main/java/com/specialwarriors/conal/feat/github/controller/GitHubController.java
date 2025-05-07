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


    @GetMapping("/user/{owner}")
    public Flux<Map<String, Object>> getUser(@PathVariable String owner) {
        return githubService.getUserInfo(owner);
    }

    @GetMapping("users/{owner}/repos")
    public Flux<Map<String, Object>> getRepos(@PathVariable String owner) {
        return githubService.getRepos(owner);
    }

    @GetMapping("repos/{owner}/{repo}")
    public Flux<Map<String, Object>> getRepo(@PathVariable String owner,
        @PathVariable String repo) {
        return githubService.getRepo(owner, repo);
    }

    @GetMapping("/repos/{owner}/{repo}/commits")
    public Flux<Map<String, Object>> getCommits(@PathVariable String owner,
        @PathVariable String repo) {
        return githubService.getCommit(owner, repo);
    }

    @GetMapping("/repos/{owner}/{repo}/contributors")
    public Flux<Map<String, Object>> getContributors(@PathVariable String owner,
        @PathVariable String repo
    ) {
        return githubService.getContributors(owner, repo);
    }

    @GetMapping("/repos/{owner}/{repo}/commits/{username}")
    public Flux<Map<String, Object>> getCommits(@PathVariable String owner,
        @PathVariable String repo,
        @PathVariable String username
    ) {
        return githubService.getCommits(username, owner, repo);
    }

    @GetMapping("/repos/{owner}/{repo}/pulls/{username}")
    public Flux<Map<String, Object>> getPullRequests(
        @PathVariable String owner,
        @PathVariable String repo,
        @PathVariable String username
    ) {
        return githubService.getPullRequests(owner, repo, username);
    }

    @GetMapping("/repos/{owner}/{repo}/pulls")
    public Flux<Map<String, Object>> getAllPullRequests(
        @PathVariable String owner,
        @PathVariable String repo
    ) {
        return githubService.getAllPullRequest(owner, repo);
    }

}