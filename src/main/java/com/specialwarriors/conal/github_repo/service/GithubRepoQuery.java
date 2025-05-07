package com.specialwarriors.conal.github_repo.service;

import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.exception.GithubRepoException;
import com.specialwarriors.conal.github_repo.repository.GithubRepoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GithubRepoQuery {

    private final GithubRepoRepository githubRepoRepository;

    public GithubRepo findByRepositoryId(Long githubRepoId) {
        return githubRepoRepository.findById(githubRepoId).orElseThrow(() ->
            new GeneralException(GithubRepoException.NOT_FOUND_GITHUBREPO)
        );
    }

    public GithubRepo findByUserIdAndRepositoryId(Long userId, Long repositoryId) {

        GithubRepo githubRepo = githubRepoRepository.findById(repositoryId).orElseThrow(() ->
            new GeneralException(GithubRepoException.NOT_FOUND_GITHUBREPO)
        );

        if (!userId.equals(githubRepo.getUser().getId())) {
            throw new GeneralException(GithubRepoException.UNAUTHORIZED_GITHUBREPO_ACCESS);
        }

        return githubRepo;
    }
}
