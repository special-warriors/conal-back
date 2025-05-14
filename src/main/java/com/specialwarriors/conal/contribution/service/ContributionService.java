package com.specialwarriors.conal.contribution.service;

import com.specialwarriors.conal.contribution.dto.response.ContributionFormResponse;
import com.specialwarriors.conal.contributor.domain.Contributor;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContributionService {

    public ContributionFormResponse sendEmail(Contributor contributor, GithubRepo githubRepo) {
        Long userId = githubRepo.getUser().getId();
        Long repoId = githubRepo.getId();

        return new ContributionFormResponse(userId, repoId,
            contributor.getEmail());
    }


}
