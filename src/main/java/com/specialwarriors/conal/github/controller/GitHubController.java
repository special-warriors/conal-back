package com.specialwarriors.conal.github.controller;

import com.specialwarriors.conal.github.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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


    @PostMapping("/repos/{owner}/{repo}/ranking")
    public Mono<ResponseEntity<String>> updateAllGithubContributorRanks(
        @PathVariable String owner,
        @PathVariable String repo
    ) {
        return githubService.updateRepoRanking(owner, repo)
            .thenReturn(ResponseEntity.ok("전체 랭킹 업데이트 완료"));
    }

}