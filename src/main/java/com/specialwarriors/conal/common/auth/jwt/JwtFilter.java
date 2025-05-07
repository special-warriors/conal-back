package com.specialwarriors.conal.common.auth.jwt;

import com.specialwarriors.conal.common.auth.exception.AuthException;
import com.specialwarriors.conal.common.auth.exception.CustomAuthException;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        if (isPermitAllPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

//        // ✅ 개발 편의: userId가 1이면 강제로 인증 처리
//        if (Objects.equals(userId, 1L)) {
//            UserDetails userDetails = userDetailsService.loadUserByUserId(1L);
//
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(userDetails, null,
//                            userDetails.getAuthorities());
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }

        try {
            String token = resolveToken(request);

            if (jwtProvider.validateToken(token)) {
                Long userId = jwtProvider.getUserId(token);

                UserDetails userDetails = userDetailsService.loadUserByUserId(userId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null,
                                userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (RuntimeException e) {
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

    private boolean isPermitAllPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PERMIT_ALL_PATHS.contains(path);
    }

    // TODO : 기능 추가되면 인증 필요없는 URI 추가
    private static final List<String> PERMIT_ALL_PATHS = List.of(
            "/"
    );
}
