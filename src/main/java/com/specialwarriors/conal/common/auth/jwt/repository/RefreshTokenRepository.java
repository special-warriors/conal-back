package com.specialwarriors.conal.common.auth.jwt.repository;

import com.specialwarriors.conal.common.auth.jwt.domain.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findRefreshTokenByUserId(Long userId);
}
