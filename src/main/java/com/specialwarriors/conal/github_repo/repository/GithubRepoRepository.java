package com.specialwarriors.conal.github_repo.repository;

import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GithubRepoRepository extends JpaRepository<GithubRepo, Long> {

}
