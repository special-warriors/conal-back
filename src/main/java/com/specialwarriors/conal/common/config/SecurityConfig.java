package com.specialwarriors.conal.common.config;

import com.specialwarriors.conal.common.auth.oauth.CustomLogoutHandler;
import com.specialwarriors.conal.common.auth.oauth.CustomOAuth2FailureHandler;
import com.specialwarriors.conal.common.auth.oauth.CustomOAuth2SuccessHandler;
import com.specialwarriors.conal.common.auth.oauth.CustomOAuth2UserService;
import com.specialwarriors.conal.common.auth.session.SessionManager;
import com.specialwarriors.conal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/**")
                        .permitAll()
                        .anyRequest()
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserService())
                        )
                        .successHandler(
                                authenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                )
                .csrf(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(logoutHandler())
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {

        return new CustomOAuth2UserService(userRepository);
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {

        return new CustomOAuth2SuccessHandler(userRepository, sessionManager, redisTemplate,
                authorizedClientService);
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {

        return new CustomOAuth2FailureHandler();
    }

    @Bean
    public LogoutHandler logoutHandler() {

        return new CustomLogoutHandler(sessionManager);
    }
}