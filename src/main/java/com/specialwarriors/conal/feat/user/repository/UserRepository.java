package com.specialwarriors.conal.feat.user.repository;

import com.specialwarriors.conal.feat.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByGithubId(Long githubId);

    boolean existsByGithubId(Long githubId);
}
