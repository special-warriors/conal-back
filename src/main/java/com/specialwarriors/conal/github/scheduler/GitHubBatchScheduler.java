package com.specialwarriors.conal.github.scheduler;

import com.specialwarriors.conal.github.service.GitHubService;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.repository.GithubRepoRepository;
import com.specialwarriors.conal.github_repo.util.UrlUtil;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubBatchScheduler {

    private final GithubRepoRepository githubRepoRepository;
    private final GitHubService gitHubService;

    @PostConstruct
    public void init() {
        scheduledUpdate();
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void scheduledUpdate() {

        List<GithubRepo> githubRepos = githubRepoRepository.findAll();
        List<String[]> reposToUpdate = githubRepos.stream()
            .map(repo -> UrlUtil.urlToOwnerAndReponame(repo.getUrl()))
            .toList();

        Flux.fromIterable(reposToUpdate)
            .flatMap(arr -> gitHubService.updateRepoContribution(arr[0], arr[1]))
            .subscribe();
    }

}
