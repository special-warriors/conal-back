package com.specialwarriors.conal.common.auth.jwt;

import com.specialwarriors.conal.common.auth.exception.AuthException;
import com.specialwarriors.conal.common.auth.exception.CustomAuthException;
import com.specialwarriors.conal.common.auth.jwt.domain.RefreshToken;
import com.specialwarriors.conal.common.auth.jwt.repository.RefreshTokenRepository;
import com.specialwarriors.conal.common.auth.oauth.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    // TODO : RefreshToken Redis 마이그레이션, 리팩토링
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        log.info("[JWT] jwt 검증 시작");

        // ✅ 개발 편의 모드: userId=1 강제 인증
        if (true) {
            log.warn("[JWT] 개발 편의 모드 - userId=1 강제 인증");

            UserDetails userDetails = userDetailsService.loadUserByUserId(1L);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null,
                            userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = resolveToken(request);

        if (accessToken == null) {
            throw new CustomAuthException(AuthException.INVALID_TOKEN);
        }
        if (jwtProvider.validateToken(accessToken)) {
            Long userId = jwtProvider.getUserId(accessToken);
            UserDetails userDetails = userDetailsService.loadUserByUserId(userId);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null,
                            userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } else {
            Long userId = jwtProvider.getUserId(accessToken);

            RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomAuthException(AuthException.INVALID_TOKEN));

            if (jwtProvider.isExpired(savedToken.getRefreshToken())) {
                response.sendRedirect("/");
                throw new CustomAuthException(AuthException.EXPIRED_TOKEN);
            }
            String newAccessToken = jwtProvider.createAccessToken(userId);
            response.setHeader("Authorization", "Bearer " + newAccessToken);
            UserDetails userDetails = userDetailsService.loadUserByUserId(userId);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null,
                            userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            throw new CustomAuthException(AuthException.EMPTY_TOKEN); // TODO : 예외 처리
        }
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return true;
    }

    // TODO : 기능 추가되면 인증 필요없는 URI 추가
    private static final List<String> PERMIT_ALL_PATHS = List.of(
            "/", "/login"
    );
}
