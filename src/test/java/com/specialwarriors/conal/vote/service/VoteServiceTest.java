package com.specialwarriors.conal.vote.service;

import static com.specialwarriors.conal.github_repo.exception.GithubRepoException.GITHUB_REPO_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
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
import com.specialwarriors.conal.vote.dto.request.VoteSubmitRequest;
import com.specialwarriors.conal.vote.dto.response.VoteFormResponse;
import com.specialwarriors.conal.vote.dto.response.VoteResultResponse;
import com.specialwarriors.conal.vote.exception.VoteException;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    private final long REPO_ID = 1L;
    private final List<String> EMAILS = List.of("mj3242@naver.com",
            "mj1111@naver.com",
            "mj2222@naver.com",
            "mj3333@naver.com");
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
            for (int i = 0; i < contributors.size(); i++) {
                when(contributors.get(i).getEmail()).thenReturn(EMAILS.get(i));
            }

            when(githubRepo.getContributors()).thenReturn(contributors);

            when(jwtTokenProvider.createVoteUserToken(any(String.class),
                    any(Date.class), anyLong()))
                    .thenReturn(USER_TOKEN);

            SetOperations<String, String> setOperations = mock(SetOperations.class);
            when(redisTemplate.opsForSet()).thenReturn(setOperations);

            // when
            voteService.openVote(REPO_ID);

            // then
            verify(githubRepoQuery).findById(REPO_ID);
            verify(jwtTokenProvider, times(contributors.size()))
                    .createVoteUserToken(any(String.class), any(Date.class), anyLong());
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

            Set<String> userTokens = IntStream.range(0, 4)
                    .mapToObj(i -> USER_TOKEN)
                    .collect(Collectors.toSet());
            SetOperations<String, String> setOperations = mock(SetOperations.class);
            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.members(any(String.class))).thenReturn(userTokens);

            GithubRepo githubRepo = mock(GithubRepo.class);
            when(githubRepoQuery.findById(REPO_ID)).thenReturn(githubRepo);

            List<Contributor> contributors = IntStream.range(0, 4)
                    .mapToObj(i -> mock(Contributor.class))
                    .toList();
            for (int i = 0; i < contributors.size(); i++) {
                when(contributors.get(i).getEmail()).thenReturn(EMAILS.get(i));
            }
            when(githubRepo.getContributors()).thenReturn(contributors);

            // when
            List<String> emails = voteService.findVoteTargetEmails(REPO_ID, USER_TOKEN);

            // then
            assertThat(emails).hasSize(contributors.size())
                    .containsAll(EMAILS);
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

            Set<String> userTokens = Collections.EMPTY_SET;
            SetOperations<String, String> setOperations = mock(SetOperations.class);
            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.members(any(String.class))).thenReturn(userTokens);

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

    @Nested
    @DisplayName("투표 폼 구성 데이터 제공 시 ")
    class GetVoteFormResponseTest {

        @DisplayName("성공한다.")
        @Test
        public void success() {
            // given
            when(redisTemplate.hasKey(any(String.class))).thenReturn(true);

            Set<String> userTokens = new HashSet<>();
            for (int i = 0; i < EMAILS.size(); i++) {
                String userToken = USER_TOKEN + i;
                userTokens.add(userToken);
                when(jwtTokenProvider.extractEmailFrom(userToken)).thenReturn(EMAILS.get(i));
            }

            SetOperations<String, String> setOperations = mock(SetOperations.class);
            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.members(any(String.class))).thenReturn(userTokens);

            // when
            List<VoteFormResponse> responses = voteService.getVoteFormResponse(REPO_ID);

            // then
            assertThat(responses).hasSize(userTokens.size())
                    .allMatch(response -> response.repoId() == REPO_ID)
                    .allMatch(response -> EMAILS.contains(response.email()))
                    .allMatch(response -> userTokens.contains(response.userToken()))
                    .allMatch(response -> EMAILS.containsAll(response.voteTargetEmails()));
        }

        @DisplayName("투표 오픈 내역이 존재하지 않을 경우 예외가 발생한다.")
        @Test
        public void voteNotFound() {
            // given
            when(redisTemplate.hasKey(any(String.class))).thenReturn(false);

            // when

            // then
            assertThatThrownBy(() -> voteService.getVoteFormResponse(REPO_ID))
                    .isInstanceOf(GeneralException.class)
                    .extracting("exception")
                    .isEqualTo(VoteException.VOTE_NOT_FOUND);
        }

        @DisplayName("유효하지 않은 사용자 토큰이 존재할 경우 예외가 발생한다.")
        @Test
        public void invalidUserToken() {
            // given
            when(redisTemplate.hasKey(any(String.class))).thenReturn(true);

            Set<String> userTokens = IntStream.range(0, 4)
                    .mapToObj(i -> USER_TOKEN + i)
                    .collect(Collectors.toSet());
            SetOperations<String, String> setOperations = mock(SetOperations.class);
            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.members(any(String.class))).thenReturn(userTokens);

            when(jwtTokenProvider.extractEmailFrom(any(String.class)))
                    .thenThrow(new JwtException("invalid user token"));

            // when

            // then
            assertThatThrownBy(() -> voteService.getVoteFormResponse(REPO_ID))
                    .isInstanceOf(JwtException.class);
        }
    }

    @Nested
    @DisplayName("투표 요청 저장 시 ")
    class SaveVoteRequestTest {

        @DisplayName("성공한다.")
        @Test
        public void success() {
            // given
            when(redisTemplate.hasKey(any(String.class))).thenReturn(true);

            Set<String> userTokens = IntStream.range(0, 4)
                    .mapToObj(i -> USER_TOKEN + i)
                    .collect(Collectors.toSet());
            SetOperations<String, String> setOperations = mock(SetOperations.class);
            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.members(any(String.class))).thenReturn(userTokens);

            HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
            when(hashOperations.hasKey(any(String.class), any(String.class))).thenReturn(false);
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);

            String userToken = userTokens.stream().findFirst().get();
            String votedEmail = EMAILS.get(0);
            VoteSubmitRequest request = new VoteSubmitRequest(REPO_ID, userToken, votedEmail);

            // when
            boolean result = voteService.saveVoteRequest(REPO_ID, request);

            // then
            assertThat(result).isTrue();
            verify(hashOperations).put(any(String.class), eq(userToken), eq(votedEmail));
            verify(redisTemplate).expire(any(String.class), any(Duration.class));
        }

        @DisplayName("이미 투표한 경우 false를 반환한다.")
        @Test
        public void alreadyVoted() {
            // given
            when(redisTemplate.hasKey(any(String.class))).thenReturn(true);

            Set<String> userTokens = IntStream.range(0, 4)
                    .mapToObj(i -> USER_TOKEN + i)
                    .collect(Collectors.toSet());
            SetOperations<String, String> setOperations = mock(SetOperations.class);
            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.members(any(String.class))).thenReturn(userTokens);

            HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
            when(hashOperations.hasKey(any(String.class), any(String.class))).thenReturn(true);
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);

            String userToken = userTokens.stream().findFirst().get();
            String votedEmail = EMAILS.get(0);
            VoteSubmitRequest request = new VoteSubmitRequest(REPO_ID, userToken, votedEmail);

            // when
            boolean result = voteService.saveVoteRequest(REPO_ID, request);

            // then
            assertThat(result).isFalse();
        }

        @DisplayName("투표 오픈 내역이 존재하지 않을 경우 예외가 발생한다.")
        @Test
        public void voteNotFound() {
            // given
            when(redisTemplate.hasKey(any(String.class))).thenReturn(false);

            String votedEmail = EMAILS.get(0);
            VoteSubmitRequest request = new VoteSubmitRequest(REPO_ID, USER_TOKEN, votedEmail);

            // when

            // then
            assertThatThrownBy(() -> voteService.saveVoteRequest(REPO_ID, request))
                    .isInstanceOf(GeneralException.class)
                    .extracting("exception")
                    .isEqualTo(VoteException.VOTE_NOT_FOUND);
        }

        @DisplayName("투표에 접근할 수 없는 사용자일 경우 예외가 발생한다.")
        @Test
        public void unauthorizedVoteAccess() {
            // given
            when(redisTemplate.hasKey(any(String.class))).thenReturn(true);

            Set<String> userTokens = Collections.EMPTY_SET;
            SetOperations<String, String> setOperations = mock(SetOperations.class);
            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.members(any(String.class))).thenReturn(userTokens);

            String votedEmail = EMAILS.get(0);
            VoteSubmitRequest request = new VoteSubmitRequest(REPO_ID, USER_TOKEN, votedEmail);

            // when

            // then
            assertThatThrownBy(() -> voteService.saveVoteRequest(REPO_ID, request))
                    .isInstanceOf(GeneralException.class)
                    .extracting("exception")
                    .isEqualTo(VoteException.UNAUTHORIZED_VOTE_ACCESS);
        }
    }

    @DisplayName("투표 결과를 저장할 때 성공한다.")
    @Test
    public void saveVoteResult() {
        // given
        VoteSubmitRequest request = new VoteSubmitRequest(REPO_ID, USER_TOKEN, EMAILS.get(0));

        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        // when
        voteService.saveVoteResult(REPO_ID, request);

        // then
        verify(hashOperations).increment(any(String.class), eq(request.votedEmail()), eq(1L));
        verify(redisTemplate).expire(any(String.class), any(Duration.class));
    }

    @Nested
    @DisplayName("투표 결과를 조회할 때 ")
    class GetVoteResultTest {

        @DisplayName("성공한다.")
        @Test
        public void success() {
            // given
            Map<Object, Object> entries = Map.of(
                    EMAILS.get(0), 1,
                    EMAILS.get(1), 0,
                    EMAILS.get(2), 1,
                    EMAILS.get(3), 2
            );

            HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            when(hashOperations.entries(any(String.class))).thenReturn(entries);

            // when
            VoteResultResponse response = voteService.getVoteResult(REPO_ID);

            // then
            assertThat(response.items()).hasSize(entries.size())
                    .extracting("email", "votes")
                    .containsExactlyInAnyOrder(tuple(EMAILS.get(0), 1),
                            tuple(EMAILS.get(1), 0),
                            tuple(EMAILS.get(2), 1),
                            tuple(EMAILS.get(3), 2));
        }

        @DisplayName("투표 참여 내역이 없을 경우 빈 List를 포함한 응답을 반환한다.")
        @Test
        public void noVoteParticipants() {
            // given
            HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            when(hashOperations.entries(any(String.class))).thenReturn(new HashMap<>());

            // when
            VoteResultResponse response = voteService.getVoteResult(REPO_ID);

            // then
            assertThat(response.items()).isEmpty();
        }
    }
}
