package com.specialwarriors.conal.vote.service;

import com.specialwarriors.conal.common.auth.jwt.JwtTokenProvider;
import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.contributor.domain.Contributor;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.service.GithubRepoQuery;
import com.specialwarriors.conal.vote.dto.request.VoteSubmitRequest;
import com.specialwarriors.conal.vote.dto.response.VoteFormResponse;
import com.specialwarriors.conal.vote.exception.VoteException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final String VOTE_OPEN_KEY_FORMAT = "vote:open:%s";
    private final Duration VOTE_EXPIRATION = Duration.ofDays(7);

    private final RedisTemplate<String, String> redisTemplate;
    private final GithubRepoQuery githubRepoQuery;
    private final JwtTokenProvider jwtProvider;

    public void openVote(long repoId) {
        String voteKey = VOTE_OPEN_KEY_FORMAT.formatted(repoId);

        // 투표 참여자를 고유하게 식별한 토큰 생성
        final Date issuedAt = new Date();
        final long expirationMillis = 604800000;

        GithubRepo githubRepo = githubRepoQuery.findByRepositoryId(repoId);
        List<Contributor> contributors = githubRepo.getContributors();

        String[] userTokens = contributors.stream().map(Contributor::getEmail)
                .map(email -> jwtProvider.createVoteUserToken(email, issuedAt,
                        expirationMillis))
                .toArray(String[]::new);

        redisTemplate.opsForSet().add(voteKey, userTokens);
        redisTemplate.expire(voteKey, VOTE_EXPIRATION);
    }

    public List<String> findVoteTargetEmails(long repoId, String userToken) {
        String voteKey = VOTE_OPEN_KEY_FORMAT.formatted(repoId);

        if (!redisTemplate.hasKey(voteKey)) {
            throw new GeneralException(VoteException.VOTE_NOT_FOUND);
        }

        Set<String> userTokens = redisTemplate.opsForSet().members(voteKey);
        if (!userTokens.contains(userToken)) {
            throw new GeneralException(VoteException.UNAUTHORIZED_VOTE_ACCESS);
        }

        GithubRepo githubRepo = githubRepoQuery.findByRepositoryId(repoId);

        return githubRepo.getContributors().stream()
                .map(Contributor::getEmail)
                .toList();
    }

    public List<VoteFormResponse> getVoteFormResponse(long repoId) {
        String voteKey = VOTE_OPEN_KEY_FORMAT.formatted(repoId);

        // 존재하는 투표인지 검증
        if (!redisTemplate.hasKey(voteKey)) {
            throw new GeneralException(VoteException.VOTE_NOT_FOUND);
        }

        Set<String> userTokens = redisTemplate.opsForSet().members(voteKey);
        List<String> voteTargetEmails = userTokens.stream().map(jwtProvider::getEmailFrom).toList();

        return userTokens.stream().map(userToken -> {
                    String email = jwtProvider.getEmailFrom(userToken);

                    return new VoteFormResponse(repoId, userToken, email, voteTargetEmails);
                })
                .toList();
    }

    public boolean saveVoteRequest(long repoId, VoteSubmitRequest request) {
        String voteKey = VOTE_OPEN_KEY_FORMAT.formatted(repoId);

        // 존재하는 투표인지 검증
        if (!redisTemplate.hasKey(voteKey)) {
            throw new GeneralException(VoteException.VOTE_NOT_FOUND);
        }

        // 사용자가 접근 가능한 투표인지 검증
        Set<String> userTokens = redisTemplate.opsForSet().members(voteKey);
        String userToken = request.userToken();
        if (!userTokens.contains(userToken)) {
            throw new GeneralException(VoteException.UNAUTHORIZED_VOTE_ACCESS);
        }

        final String voteReqFormat = "vote:req:%s:%s";
        String key = voteReqFormat.formatted(repoId, userToken);

        if (redisTemplate.opsForHash().hasKey(key, userToken)) {

            return false;
        }

        redisTemplate.opsForHash().put(key, userToken, request.votedEmail());
        redisTemplate.expire(key, VOTE_EXPIRATION);

        return true;
    }

    public void saveVoteResult(long repoId, VoteSubmitRequest request) {
        final String votedEmail = request.votedEmail();
        final String voteResFormat = "vote:res:%s:%s";
        String key = voteResFormat.formatted(repoId, votedEmail);

        redisTemplate.opsForHash().increment(key, votedEmail, 1);
        redisTemplate.expire(key, VOTE_EXPIRATION);
    }
}
