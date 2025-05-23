package com.specialwarriors.conal.common.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.JwtException;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private final String JWT_SECRET = "sWfMxuTSQ4BzujHmMw71u96o+TUlanQqPIqxGBHfPz0=";
    private final String EMAIL = "email@example.com";
    private final Date ISSUED_AT = new Date();
    private final long EXPIRATION_MILLIS = 604800000;

    private JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(JWT_SECRET);

    @DisplayName("투표 사용자 토큰을 생성할 수 있다.")
    @Test
    public void createVoteUserToken() {
        // given

        // when

        // then
        assertThatNoException().isThrownBy(() -> jwtTokenProvider
                        .createVoteUserToken(EMAIL, ISSUED_AT, EXPIRATION_MILLIS));
    }

    @Nested
    @DisplayName("투표 사용자 토큰에서 이메일을 추출할 때")
    class ExtractEmailFromTest {

        @DisplayName("성공한다.")
        @Test
        public void success() {
            // given
            String userToken = jwtTokenProvider
                    .createVoteUserToken(EMAIL, ISSUED_AT, EXPIRATION_MILLIS);

            // when
            String extractedEmail = jwtTokenProvider.extractEmailFrom(userToken);

            // then
            assertThat(extractedEmail).isEqualTo(EMAIL);
        }

        @DisplayName("유효하지 않은 포맷의 토큰일 경우 예외가 발생한다.")
        @Test
        public void invalidFormatToken() {
            // given
            String invalidToken = "invalidToken";

            // when

            // then
            assertThatThrownBy(() -> jwtTokenProvider.extractEmailFrom(invalidToken))
                    .isInstanceOf(JwtException.class);
        }

        @DisplayName("만료된 토큰일 경우 예외가 발생한다.")
        @Test
        public void expiredToken() {
            // given
            long issuedAtMillis = ISSUED_AT.toInstant()
                    .minusMillis(EXPIRATION_MILLIS + 1)
                    .toEpochMilli();
            Date issuedAt = new Date(issuedAtMillis);

            String expiredToken = jwtTokenProvider
                    .createVoteUserToken(EMAIL, issuedAt, EXPIRATION_MILLIS);

            // when

            // then
            assertThatThrownBy(() -> jwtTokenProvider.extractEmailFrom(expiredToken))
                    .isInstanceOf(JwtException.class);
        }
    }
}
