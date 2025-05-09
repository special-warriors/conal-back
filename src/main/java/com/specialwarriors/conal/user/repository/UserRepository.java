package com.specialwarriors.conal.user.repository;


import com.specialwarriors.conal.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByGithubId(Long githubId);

    boolean existsByGithubId(Long githubId);
}
