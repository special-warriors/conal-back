package com.specialwarriors.conal.github_repo.controller;

import com.specialwarriors.conal.github_repo.dto.request.GithubRepoCreateRequest;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoCreateResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoDeleteResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoGetResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoPageResponse;
import com.specialwarriors.conal.github_repo.service.GithubRepoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/users/{userId}/repositories")
@RequiredArgsConstructor
public class GithubRepoRestController {

    private final GithubRepoService githubRepoService;

    @PostMapping
    public Mono<GithubRepoCreateResponse> createGitHubRepo(@PathVariable Long userId,
        @RequestBody GithubRepoCreateRequest request) {

        GithubRepoCreateResponse response = githubRepoService.createGithubRepo(userId, request);
        return Mono.just(response);
    }

    @GetMapping
    public Mono<GithubRepoPageResponse> getGithubRepos(@PathVariable long userId,
        @RequestParam int page) {

        return Mono.fromCallable(() -> githubRepoService.getGithubRepoInfos(userId, page))
            .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{repositoryId}")
    public Mono<GithubRepoGetResponse> getRepositoryId(@PathVariable long userId,
        @PathVariable long repositoryId) {

        GithubRepoGetResponse response = githubRepoService.getGithubRepoInfo(userId, repositoryId);
        return Mono.just(response);
    }

    @DeleteMapping("/{repositoryId}")
    public GithubRepoDeleteResponse deleteResponse(@PathVariable long userId,
        @PathVariable long repositoryId) {
        return githubRepoService.deleteRepo(userId, repositoryId);
    }

}
