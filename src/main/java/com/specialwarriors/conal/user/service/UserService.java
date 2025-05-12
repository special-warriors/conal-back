package com.specialwarriors.conal.user.service;

import com.specialwarriors.conal.common.auth.oauth.CustomOAuth2UserService;
import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.exception.UserException;
import com.specialwarriors.conal.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final CustomOAuth2UserService customOAuth2UserService;

    public User getUserByUserId(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND));
    }

    public void deleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND));

        String oAuthAccessToken = redisTemplate.opsForValue()
                .get("github:token:" + user.getGithubId());
        customOAuth2UserService.revokeUser(oAuthAccessToken);

        Optional.of(user.getGithubId())
                .ifPresent(id -> redisTemplate.delete("github:token:" + id));

        userRepository.deleteById(userId);
    }
}
