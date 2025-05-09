package com.specialwarriors.conal.user.service;

import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserByUserId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 사용자를 찾을 수 없습니다. userId = " + userId));
    }
}
