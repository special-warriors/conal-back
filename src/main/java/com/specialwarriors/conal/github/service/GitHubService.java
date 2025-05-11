package com.specialwarriors.conal.github.service;

import com.specialwarriors.conal.github.scheduler.GitHubBatchScheduler;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final GitHubBatchScheduler githubBatchScheduler;

    public Mono<Void> updateRepoRanking(String owner, String repo) {

        return githubBatchScheduler.updateRepoRanking(owner, repo);
    }

    /**
     * owner/repo에 속한 모든 contributor 목록 가져오기
     */
    public Mono<List<String>> getContributors(String owner, String repo) {
        String key = "contributors:" + owner + ":" + repo;

        return reactiveRedisTemplate.opsForList()
            .range(key, 0, -1)
            .collectList();
    }

    /**
     * 특정 contributor에 대한 상세 점수 정보 가져오기
     */
    public Mono<Map<String, String>> getContributorDetail(String owner, String repo,
        String contributor) {
        String detailKey = "detail:" + owner + ":" + repo + ":" + contributor;

        return reactiveRedisTemplate.opsForHash()
            .entries(detailKey)
            .collectMap(
                e -> e.getKey().toString(),
                e -> e.getValue().toString()
            );
    }

}
