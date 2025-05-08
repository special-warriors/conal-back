package com.specialwarriors.conal.common.auth.controller;

import com.specialwarriors.conal.common.auth.jwt.JwtTokenResponse;
import com.specialwarriors.conal.common.auth.jwt.dto.TokenReissueRequest;
import com.specialwarriors.conal.common.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<JwtTokenResponse> reissue(@RequestBody TokenReissueRequest request) {
        JwtTokenResponse tokens = authService.reissueToken(request.refreshToken());
        return ResponseEntity.ok(tokens);
    }
}