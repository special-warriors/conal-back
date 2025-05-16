package com.specialwarriors.conal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.specialwarriors.conal.common.auth.oauth.GithubOAuth2WebClient;
import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.github_repo.repository.GithubRepoRepository;
import com.specialwarriors.conal.notification.repository.NotificationAgreementRepository;
import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.exception.UserException;
import com.specialwarriors.conal.user.repository.UserRepository;
import com.specialwarriors.conal.user.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private GithubOAuth2WebClient githubOAuth2WebClient;

    @Mock
    private GithubRepoRepository githubRepoRepository;

    @Mock
    private NotificationAgreementRepository notificationAgreementRepository;
    
    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User(1, "홍길동", "honggildong@gmail.com");
    }

    @Nested
    @DisplayName("유저 아이디로 사용자를 조회할 때")
    class FindUserByIdTest {

        @Test
        @DisplayName("성공한다")
        void success() {

            // given
            long userId = 1L;
            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

            // when
            User result = userService.findById(userId);

            // then
            assertThat(result).isEqualTo(mockUser);
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("시용자가 존재하지 않으면 예외를 던진다")
        void throwsExceptionWhenUserNotFoundById() {

            // given
            long userId = 1L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when, then
            assertThatThrownBy(() -> userService.findById(userId))
                    .isInstanceOf(GeneralException.class)
                    .extracting("exception")
                    .isEqualTo(UserException.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("사용자를 삭제할 때")
    class DeleteUserTest {

        @Test
        @DisplayName("깃허브 토큰과 세션을 삭제한다")
        void deletesGithubTokenAndSessionWhenUserIsDeleted() {

            // given
            long userId = 1L;
            String githubTokenKey = "github:token:1";
            String githubTokenValue = "fake-access-token";

            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

            ValueOperations<String, String> ops = mock(ValueOperations.class);
            given(redisTemplate.opsForValue()).willReturn(ops);
            given(ops.get(githubTokenKey)).willReturn(githubTokenValue);

            // when
            userService.deleteById(userId);

            // then
            verify(githubOAuth2WebClient).unlink(githubTokenValue);
            verify(redisTemplate).unlink(githubTokenKey);
            verify(userRepository).deleteById(userId);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외를 던진다")
        void throwsExceptionWhenDeletingNonexistentUser() {

            // given
            long userId = 1L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when, then
            assertThatThrownBy(() -> userService.deleteById(userId))
                    .isInstanceOf(GeneralException.class)
                    .extracting("exception")
                    .isEqualTo(UserException.USER_NOT_FOUND);
        }
    }

}
