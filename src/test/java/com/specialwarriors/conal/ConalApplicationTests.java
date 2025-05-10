package com.specialwarriors.conal;

import com.specialwarriors.conal.common.auth.jwt.JwtTokenProvider;
import com.specialwarriors.conal.common.config.WebClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;

@ActiveProfiles("test")
@SpringBootTest
class ConalApplicationTests {

    @MockitoBean
    private WebClientConfig webClientConfig;

    @MockitoBean
    private WebClient webClient;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void contextLoads() {
    }
}
