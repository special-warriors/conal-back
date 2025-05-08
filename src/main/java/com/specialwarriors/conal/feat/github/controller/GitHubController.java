package com.specialwarriors.conal.feat.github.controller;

import com.specialwarriors.conal.feat.github.dto.CommitRank;
import com.specialwarriors.conal.feat.github.dto.response.CommitCountResponse;
import com.specialwarriors.conal.feat.github.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService githubService;


    @GetMapping("/repos/{owner}/{repo}/commits/{username}")
    public Mono<CommitCountResponse> getCommitCountFromCommitsApi(@PathVariable String owner,
        @PathVariable String repo,
        @PathVariable String username) {

        return githubService.getCommitCountFromCommitsApi(owner, repo, username);
    }

    @GetMapping("/repos/{repo}/users/{username}/rank")
    public Mono<CommitRank> getUserRank(
        @PathVariable String repo,
        @PathVariable String username
    ) {
        return githubService.getUserCommitRank(repo, username);
    }

    @PostMapping("/repos/{owner}/{repo}/users/{username}/update")
    public Mono<Void> updateCommitRanking(
        @PathVariable String owner,
        @PathVariable String repo,
        @PathVariable String username
    ) {
        return githubService.updateCommitRanking(owner, repo, username);
    }

}