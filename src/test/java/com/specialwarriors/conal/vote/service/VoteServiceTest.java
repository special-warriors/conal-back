package com.specialwarriors.conal.vote.service;

import static com.specialwarriors.conal.github_repo.exception.GithubRepoException.GITHUB_REPO_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.specialwarriors.conal.common.auth.jwt.JwtTokenProvider;
import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.contributor.domain.Contributor;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.service.GithubRepoQuery;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

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

        private final long REPO_ID = 1L;

        @DisplayName("성공한다.")
        @Test
        public void success() {
            // given
            GithubRepo githubRepo = Mockito.mock(GithubRepo.class);
            when(githubRepoQuery.findById(REPO_ID)).thenReturn(githubRepo);

            String email = "ex@example.com";
            List<Contributor> contributors = IntStream.range(0, 4)
                    .mapToObj(i -> Mockito.mock(Contributor.class))
                    .toList();
            contributors.forEach(contributor -> when(contributor.getEmail())
                    .thenReturn(email));
            when(githubRepo.getContributors()).thenReturn(contributors);

            String userToken = "header.payload.signature";
            when(jwtTokenProvider.createVoteUserToken(eq(email), any(Date.class), anyLong()))
                    .thenReturn(userToken);

            SetOperations<String, String> setOperations = Mockito.mock(SetOperations.class);
            when(redisTemplate.opsForSet()).thenReturn(setOperations);

            // when
            voteService.openVote(REPO_ID);

            // then
            verify(githubRepoQuery).findById(REPO_ID);
            verify(jwtTokenProvider, times(contributors.size()))
                    .createVoteUserToken(eq(email), any(Date.class), anyLong());
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
}