package com.specialwarriors.conal.github_repo.repository;

import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GithubRepoRepositoryCustom {

    Page<GithubRepo> findGithubRepoPages(Long userId, Pageable pageable);
}
