package com.specialwarriors.conal.vote.service;

import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.vote.dto.request.VoteSaveRequest;
import com.specialwarriors.conal.vote.exception.VoteException;
import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final Duration VOTE_REQ_EXPIRATION = Duration.ofDays(7);

    private final RedisTemplate<String, String> redisTemplate;

    public boolean saveVoteRequest(long repoId, String voteUuid, String userToken,
            VoteSaveRequest request) {
        String voteKey = "vote:open:%s".formatted(repoId);

        // 존재하는 투표인지 검증
        if (!redisTemplate.hasKey(voteKey)) {
            throw new GeneralException(VoteException.VOTE_NOT_FOUND);
        }

        // 사용자가 접근 가능한 투표인지 검증
        Set<String> userTokens = redisTemplate.opsForSet().members(voteKey);
        if (!userTokens.contains(userToken)) {
            throw new GeneralException(VoteException.UNAUTHORIZED_VOTE_ACCESS);
        }

        final String voteReqFormat = "vote:req:%s:%s";
        String key = voteReqFormat.formatted(repoId, voteUuid);

        if (redisTemplate.opsForHash().hasKey(key, userToken)) {

            return false;
        }

        redisTemplate.opsForHash().put(key, userToken, request.votedEmail());
        redisTemplate.expire(key, VOTE_REQ_EXPIRATION);

        return true;
    }

    public void saveVoteResult(long repoId, String voteUuid, VoteSaveRequest request) {
        final String voteResFormat = "vote:res:%s:%s";
        String key = voteResFormat.formatted(repoId, voteUuid);

        redisTemplate.opsForHash().increment(key, request.votedEmail(), 1);
        redisTemplate.expire(key, VOTE_REQ_EXPIRATION);
    }
}
