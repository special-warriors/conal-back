package com.specialwarriors.conal.github_repo.repository;


import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GithubRepoRepository extends JpaRepository<GithubRepo, Long> {

}
