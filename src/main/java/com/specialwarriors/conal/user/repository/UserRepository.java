package com.specialwarriors.conal.user.repository;


import com.specialwarriors.conal.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByGithubId(int githubId);

    boolean existsByGithubId(int githubId);
}
