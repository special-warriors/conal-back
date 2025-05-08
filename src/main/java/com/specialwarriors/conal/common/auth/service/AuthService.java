package com.specialwarriors.conal.common.auth.service;

import com.specialwarriors.conal.common.LogService;
import com.specialwarriors.conal.common.auth.exception.AuthException;
import com.specialwarriors.conal.common.auth.exception.CustomAuthException;
import com.specialwarriors.conal.common.auth.jwt.JwtProvider;
import com.specialwarriors.conal.common.auth.jwt.JwtTokenResponse;
import com.specialwarriors.conal.common.auth.jwt.domain.RefreshToken;
import com.specialwarriors.conal.common.auth.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LogService logService;

    public JwtTokenResponse reissueToken(String refreshToken) {
        Long userId = jwtProvider.getUserId(refreshToken);

        RefreshToken savedToken = refreshTokenRepository.findRefreshTokenByUserId(userId)
                .orElseThrow(() -> new CustomAuthException(AuthException.INVALID_TOKEN));

        if (!savedToken.getRefreshToken().equals(refreshToken)) {
            throw new CustomAuthException(AuthException.INVALID_TOKEN);
        }

        boolean isRefreshExpired = jwtProvider.isExpired(refreshToken);

        if (isRefreshExpired) {
            String newAccessToken = jwtProvider.createAccessToken(userId);
            String newRefreshToken = jwtProvider.createRefreshToken(userId);

            savedToken.updateRefreshToken(newRefreshToken);

            log.info("[{}}] accessToken과 refreshToken을 재발행했습니다", logService.getClassName());
            return new JwtTokenResponse(newAccessToken, newRefreshToken);
        }

        String newAccessToken = jwtProvider.createAccessToken(userId);
        log.info("[{}] accessToken을 재발행했습니다", logService.getClassName());
        return new JwtTokenResponse(newAccessToken, refreshToken);
    }

}
