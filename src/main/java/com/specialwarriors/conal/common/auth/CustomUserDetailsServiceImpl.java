package com.specialwarriors.conal.common.auth;

import com.specialwarriors.conal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUserId(Long userId) {
        return userRepository.findById(userId)
                .map(user -> new CustomUserDetails(
                        user.getId(),
                        user.getUsername()
                ))
                .orElseThrow(() -> new UsernameNotFoundException("NOT_FOUND_USER"));

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

}
