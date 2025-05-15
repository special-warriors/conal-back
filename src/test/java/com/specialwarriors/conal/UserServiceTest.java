package com.specialwarriors.conal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.specialwarriors.conal.common.auth.oauth.GithubOAuth2WebClient;
import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.repository.UserRepository;
import com.specialwarriors.conal.user.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User(1, "홍길동", "fdsf");
    }

    @Test
    @DisplayName("유저 아이디로 사용자를 조회할 수 있다")
    void findUserByUserId() {

        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));

        // when
        User result = userService.getUserByUserId(1L);

        // then
        assertThat(result).isEqualTo(mockUser);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("유저 아이디로 사용자 조회 시 존재하지 않으면 예외를 던진다")
    void throwsExceptionWhenUserNotFoundById() {

        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> {
            userService.getUserByUserId(1L);
        }).isInstanceOf(GeneralException.class);

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("사용자를 삭제할 때 깃허브 토큰과 세션을 삭제한다")
    void deletesGithubTokenAndSessionWhenUserIsDeleted() {

        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));

        ValueOperations<String, String> ops = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(ops);
        given(ops.get("github:token:1")).willReturn("fake-access-token");

        // when
        userService.deleteUser(1L);

        // then
        verify(githubOAuth2WebClient).unlink("fake-access-token");
        verify(redisTemplate).unlink("github:token:1");
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("사용자를 삭제하려 할 때 사용자가 존재하지 않으면 예외를 던진다")
    void throwsExceptionWhenDeletingNonexistentUser() {

        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> {
            userService.deleteUser(1L);
        }).isInstanceOf(GeneralException.class);

        verify(userRepository).findById(1L);
    }
}
