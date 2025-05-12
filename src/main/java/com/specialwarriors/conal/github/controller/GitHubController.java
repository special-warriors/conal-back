package com.specialwarriors.conal.github.controller;

import com.specialwarriors.conal.github.service.GitHubService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService githubService;

    @GetMapping("/repos/{owner}/{repo}/details")
    public Mono<Map<String, Map<String, String>>> getContributorDetailsFromRedis(
        @PathVariable String owner,
        @PathVariable String repo
    ) {
        return githubService.getContributorsFromRedis(owner, repo)
            .flatMapMany(Flux::fromIterable)
            .flatMap(login ->
                githubService.getContributorDetailFromRedis(owner, repo, login)
                    .map(detailMap -> Map.entry(login, detailMap)))
            .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    @PostMapping("/repos/{owner}/{repo}/update")
    public Mono<ResponseEntity<String>> updateAllGithubContributorAndContribution(
        @PathVariable String owner,
        @PathVariable String repo
    ) {
        return githubService.updateRepoContribution(owner, repo)
            .thenReturn(ResponseEntity.ok("전체 랭킹 업데이트 완료"));
    }

    @GetMapping("/repos/{owner}/{repo}/commits")
    public Mono<List<Map<String, String>>> getCommits(
        @PathVariable String owner,
        @PathVariable String repo,
        @RequestParam(defaultValue = "1") int page
    ) {
        return githubService.getCommits(owner, repo, page);
    }

}