package com.specialwarriors.conal.vote.service;

import static com.specialwarriors.conal.github_repo.exception.GithubRepoException.GITHUB_REPO_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.specialwarriors.conal.common.auth.jwt.JwtTokenProvider;
import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.contributor.domain.Contributor;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.service.GithubRepoQuery;
import com.specialwarriors.conal.vote.exception.VoteException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    private final long REPO_ID = 1L;
    private final String EMAIL = "ex@example.com";
    private final String USER_TOKEN = "header.payload.signature";

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private GithubRepoQuery githubRepoQuery;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private VoteService voteService;

    @Nested
    @DisplayName("투표를 오픈할 때 ")
    class OpenVoteTest {

        @DisplayName("성공한다.")
        @Test
        public void success() {
            // given
            GithubRepo githubRepo = mock(GithubRepo.class);
            when(githubRepoQuery.findById(REPO_ID)).thenReturn(githubRepo);

            List<Contributor> contributors = IntStream.range(0, 4)
                    .mapToObj(i -> mock(Contributor.class))
                    .toList();
            contributors.forEach(contributor -> when(contributor.getEmail())
                    .thenReturn(EMAIL));
            when(githubRepo.getContributors()).thenReturn(contributors);

            when(jwtTokenProvider.createVoteUserToken(eq(EMAIL), any(Date.class), anyLong()))
                    .thenReturn(USER_TOKEN);

            SetOperations<String, String> setOperations = mock(SetOperations.class);
            when(redisTemplate.opsForSet()).thenReturn(setOperations);

            // when
            voteService.openVote(REPO_ID);

            // then
            verify(githubRepoQuery).findById(REPO_ID);
            verify(jwtTokenProvider, times(contributors.size()))
                    .createVoteUserToken(eq(EMAIL), any(Date.class), anyLong());
            verify(setOperations).add(any(String.class), any(String[].class));
            verify(redisTemplate).expire(any(String.class), any(Duration.class));
        }

        @DisplayName("Github Repository가 존재하지 않을 경우 예외가 발생한다.")
        @Test
        public void githubRepoNotFound() {
            // given
            GeneralException exception = new GeneralException(GITHUB_REPO_NOT_FOUND);
            when(githubRepoQuery.findById(REPO_ID)).thenThrow(exception);

            // when

            // then
            assertThatThrownBy(() -> voteService.openVote(REPO_ID))
                    .isInstanceOf(GeneralException.class)
                    .extracting("exception")
                    .isEqualTo(GITHUB_REPO_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("투표 대상 이메일 조회 시 ")
    class FindVoteTargetEmailsTest {

        @DisplayName("성공한다.")
        @Test
        public void success() {
            // given
            when(redisTemplate.hasKey(any(String.class))).thenReturn(true);

            Set<String> userTokens = mock(Set.class);
            SetOperations<String, String> setOperations = mock(SetOperations.class);
            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.members(any(String.class))).thenReturn(userTokens);
            when(userTokens.contains(any(String.class))).thenReturn(true);

            GithubRepo githubRepo = mock(GithubRepo.class);
            when(githubRepoQuery.findById(REPO_ID)).thenReturn(githubRepo);

            List<Contributor> contributors = IntStream.range(0, 4)
                    .mapToObj(i -> mock(Contributor.class))
                    .toList();
            contributors.forEach(contributor -> when(contributor.getEmail())
                    .thenReturn(EMAIL));
            when(githubRepo.getContributors()).thenReturn(contributors);

            // when
            List<String> emails = voteService.findVoteTargetEmails(REPO_ID, USER_TOKEN);

            // then
            assertThat(emails.size()).isEqualTo(contributors.size());
            assertThat(emails).allMatch(email -> email.equals(EMAIL));
        }

        @DisplayName("투표 오픈 내역이 존재하지 않을 경우 예외가 발생한다.")
        @Test
        public void voteNotFound() {
            // given
            when(redisTemplate.hasKey(any(String.class))).thenReturn(false);

            // when

            // then

            assertThatThrownBy(() -> voteService.findVoteTargetEmails(REPO_ID, USER_TOKEN))
                    .isInstanceOf(GeneralException.class)
                    .extracting("exception")
                    .isEqualTo(VoteException.VOTE_NOT_FOUND);
        }

        @DisplayName("투표에 접근할 수 없는 사용자일 경우 예외가 발생한다.")
        @Test
        public void unauthorizedVoteAccess() {
            // given
            when(redisTemplate.hasKey(any(String.class))).thenReturn(true);

            Set<String> userTokens = mock(Set.class);
            SetOperations<String, String> setOperations = mock(SetOperations.class);
            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.members(any(String.class))).thenReturn(userTokens);
            when(userTokens.contains(any(String.class))).thenReturn(false);

            // when

            // then
            assertThatThrownBy(() -> voteService.findVoteTargetEmails(REPO_ID, USER_TOKEN))
                    .isInstanceOf(GeneralException.class)
                    .extracting("exception")
                    .isEqualTo(VoteException.UNAUTHORIZED_VOTE_ACCESS);
        }

        @DisplayName("Github Repository가 존재하지 않을 경우 예외가 발생한다.")
        @Test
        public void githubRepoNotFound() {
            // given
            when(redisTemplate.hasKey(any(String.class))).thenReturn(true);

            Set<String> userTokens = mock(Set.class);
            SetOperations<String, String> setOperations = mock(SetOperations.class);
            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.members(any(String.class))).thenReturn(userTokens);
            when(userTokens.contains(any(String.class))).thenReturn(true);

            GeneralException exception = new GeneralException(GITHUB_REPO_NOT_FOUND);
            when(githubRepoQuery.findById(REPO_ID)).thenThrow(exception);

            // when

            // then
            assertThatThrownBy(() -> voteService.findVoteTargetEmails(REPO_ID, USER_TOKEN))
                    .isInstanceOf(GeneralException.class)
                    .extracting("exception")
                    .isEqualTo(GITHUB_REPO_NOT_FOUND);
        }
    }
}
