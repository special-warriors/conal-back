package com.specialwarriors.conal.feat.github.controller;

import com.specialwarriors.conal.feat.github.service.GithubService;
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
public class GithubController {

    private final GithubService githubService;

    /**
     * 특정 레포지토리의 contributor 목록을 저장 -> List
     */
    @PostMapping("/repos/{owner}/{repo}/contributors")
    public Mono<ResponseEntity<String>> makeGithubContributors(
        @PathVariable String owner,
        @PathVariable String repo
    ) {
        return githubService.getContributorList(owner, repo)
            .thenReturn(ResponseEntity.ok("기여자 목록 저장 완료"));
    }

    /**
     * 전체 저장된 contributor 커밋 수 계산 → Redis 랭킹에 반영
     */
    @PostMapping("/repos/{owner}/{repo}/ranking")
    public Mono<ResponseEntity<String>> updateAllGithubContributorRanks(
        @PathVariable String owner,
        @PathVariable String repo
    ) {
        return githubService.updateAllRanks(owner, repo)
            .thenReturn(ResponseEntity.ok("전체 랭킹 업데이트 완료"));
    }

}
