package com.specialwarriors.conal.feat.github.service;

import com.specialwarriors.conal.feat.github.dto.CommitRank;
import com.specialwarriors.conal.feat.github.dto.response.CommitCountResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubService {

    private final WebClient githubWebClient;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    /**
     * 유저 랭킹을 가져오는 법
     */
    public Mono<CommitRank> getUserCommitRank(String repo, String username) {
        String key = "ranking:" + repo;

        Mono<Long> rankMono = reactiveRedisTemplate.opsForZSet()
            .reverseRank(key, username)
            .map(rank -> rank + 1)
            .defaultIfEmpty(-1L);

        Mono<Double> scoreMono = reactiveRedisTemplate.opsForZSet()
            .score(key, username)
            .defaultIfEmpty(0.0);

        return Mono.zip(rankMono, scoreMono)
            .map(tuple -> new CommitRank(username, tuple.getT1(), tuple.getT2().intValue()));
    }

    /**
     * 페이지 네이션 된 것들을 계산해서 update
     */
    public Mono<Void> updateCommitRanking(String owner, String repo, String username) {
        return getCommitCountFromCommitsApi(owner, repo, username)
            .flatMap(dto -> {
                String key = "ranking:" + repo;
                int score = dto.count();

                return reactiveRedisTemplate.opsForZSet()
                    .add(key, dto.githubUsername(), score)
                    .flatMap(result -> {
                        if (Boolean.TRUE.equals(result)) {
                            log.info(" 성공: [{}] {} = {}", key, dto.githubUsername(),
                                score);
                        } else {
                            log.info("실패: [{}] {}", key, dto.githubUsername());
                        }
                        return Mono.empty();
                    });
            })
            .doOnError(e -> log.error(" updateCommitRanking 실패: {}", e.getMessage(), e)).then();
    }

    /**
     * 사용자의 커밋 수를 페이지네이션으로 계산 ->
     */
    public Mono<CommitCountResponse> getCommitCountFromCommitsApi(String owner, String repo,
        String username) {
        int perPage = 100;
        return fetchCommitsPaginated(owner, repo, username, 1, perPage, 0)
            .map(totalCount -> new CommitCountResponse(username, totalCount));
    }


    /**
     * pagination으로 호출하여 전체 커밋 수 계산
     */

    private Mono<Integer> fetchCommitsPaginated(String owner, String repo, String username,
        int page, int perPage, int accumulated) {
        return githubWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/repos/{owner}/{repo}/commits")
                .queryParam("author", username)
                .queryParam("per_page", perPage)
                .queryParam("page", page)
                .build(owner, repo))
            .retrieve()
            .bodyToFlux(Map.class)
            .collectList()
            .flatMap(commitList -> {
                int currentCount = commitList.size();
                int newTotal = accumulated + currentCount;

                if (currentCount < perPage) {
                    return Mono.just(newTotal);
                } else {
                    return fetchCommitsPaginated(owner, repo, username, page + 1, perPage,
                        newTotal);
                }
            });
    }
}
