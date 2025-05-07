package com.specialwarriors.conal.common.auth.oauth;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetailsService extends UserDetailsService {

    CustomUserDetails loadUserByUserId(Long userId);
}
