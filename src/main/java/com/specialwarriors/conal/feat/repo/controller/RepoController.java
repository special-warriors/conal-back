package com.specialwarriors.conal.feat.repo.controller;

import com.specialwarriors.conal.feat.repo.dto.request.RepoCreateRequest;
import com.specialwarriors.conal.feat.repo.dto.response.RepoCreateResponse;
import com.specialwarriors.conal.feat.repo.dto.response.RepoPageResponse;
import com.specialwarriors.conal.feat.repo.service.RepoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequiredArgsConstructor
public class RepoController {

    private final RepoService repoService;

    @PostMapping("/repository")
    public Mono<RepoCreateResponse> createRepo(@RequestBody RepoCreateRequest request) {
        RepoCreateResponse response = repoService.createRepo(request);
        return Mono.just(response);
    }

    @GetMapping("/repository")
    public Mono<RepoPageResponse> getRepos(@RequestParam int page) {
        return Mono.fromCallable(() -> repoService.getRepos(page))
            .subscribeOn(Schedulers.boundedElastic());
    }

}
