package com.specialwarriors.conal.common.auth.oauth;

import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.repository.UserRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
                new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        // GitHub 사용자 정보 추출
        Number idRaw = oauth2User.getAttribute("id");
        Long githubId = idRaw.longValue();
        String username = oauth2User.getAttribute("login");
        String avatarUrl = oauth2User.getAttribute("avatar_url");

        User user = userRepository.findByGithubId(githubId)
                .orElseGet(() -> {
                    return userRepository.save(new User(githubId, username, avatarUrl));
                });

        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "login" // GitHub에서 사용자 ID로 쓰이는 필드
        );
    }
}