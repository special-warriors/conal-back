package com.specialwarriors.conal;

import com.specialwarriors.conal.common.auth.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;

@ActiveProfiles("test")
@SpringBootTest
class ConalApplicationTests {

    @MockitoBean
    private WebClient githubRevokeWebClient;

    @MockitoBean
    private WebClient githubWebClient;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void contextLoads() {
    }
}
