package com.specialwarriors.conal.github_repo.service;

import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.exception.GithubRepoException;
import com.specialwarriors.conal.github_repo.repository.GithubRepoRepository;
import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.service.UserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GithubRepoQuery {

    private final GithubRepoRepository githubRepoRepository;
    private final UserQuery userQuery;

    public GithubRepo findByUserIdAndRepositoryId(Long userId, Long repositoryId) {

        GithubRepo githubRepo = findById(repositoryId);
        User user = userQuery.findById(userId);

        if (user.notHasGithubRepo(githubRepo)) {
            throw new GeneralException(GithubRepoException.UNAUTHORIZED_GITHUB_REPO_ACCESS);
        }

        return githubRepo;
    }

    public GithubRepo findById(long repositoryId) {

        return githubRepoRepository.findById(repositoryId)
                .orElseThrow(() -> new GeneralException(GithubRepoException.GITHUB_REPO_NOT_FOUND));
    }
}
