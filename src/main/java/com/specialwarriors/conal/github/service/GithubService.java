package com.specialwarriors.conal.github.service;

import com.specialwarriors.conal.github.dto.GithubContributor;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubService {

    private static final int PER_PAGE = 100;
    private static final String RANKING_KEY_PREFIX = "ranking:";

    private final WebClient githubWebClient;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final Set<GithubContributor> contributors = new LinkedHashSet<>();

    public Mono<Void> updateAllRanks(String owner, String repo) {

        return getContributorList(owner, repo)
            .then(updateTotalContributionRanking(owner, repo));
    }

    private Mono<Void> updateTotalContributionRanking(String owner, String repo) {

        return getAllCommits(owner, repo, 1)
            .collectList()
            .flatMap(commitList -> {
                Map<String, Long> commitCountMap = countCommitsByLogin(contributors, commitList);
                return Flux.fromIterable(contributors)
                    .flatMap(contributor -> updateScoreForContributor(owner, repo, contributor,
                        commitCountMap))
                    .then(saveRankingPositionsToRedis(repo));
            });
    }

    private Mono<Void> updateScoreForContributor(String owner, String repo,
        GithubContributor contributor, Map<String, Long> commitCountMap) {
        String username = contributor.login();
        long commitCount = commitCountMap.getOrDefault(username, 0L);

        return Mono.zip(
            getPullRequestCount(owner, repo, username),
            getMergedPullRequestCount(owner, repo, username),
            getIssueCount(owner, repo, username)
        ).flatMap(tuple -> {
            long totalScore = commitCount + tuple.getT1() + tuple.getT2() + tuple.getT3();
            log.info("{} commit={}, pr={}, mpr={}, issue={} total={}", username, commitCount,
                tuple.getT1(), tuple.getT2(), tuple.getT3(), totalScore);
            return saveToRedisRanking(buildRankingKey(repo), username, totalScore);
        });
    }

    public Mono<Void> getContributorList(String owner, String repo) {

        return githubWebClient.get()
            .uri("/repos/{owner}/{repo}/contributors", owner, repo)
            .retrieve()
            .bodyToFlux(GithubContributor.class)
            .collectList()
            .doOnNext(list -> {
                contributors.clear();
                contributors.addAll(list);
                log.info("{}명: {}", contributors.size(),
                    contributors.stream().map(GithubContributor::login).toList());
            })
            .then();
    }

    private Mono<Void> saveToRedisRanking(String key, String username, long score) {

        return reactiveRedisTemplate.opsForZSet()
            .add(key, username, score)
            .doOnSuccess(
                result -> log.info("ZSet {} {} = {}", result ? "success" : "fail", username, score))
            .then();
    }

    private Mono<Void> saveRankingPositionsToRedis(String repo) {

        String key = buildRankingKey(repo);

        return reactiveRedisTemplate.opsForZSet().size(key)
            .flatMapMany(size -> reactiveRedisTemplate.opsForZSet()
                .reverseRangeWithScores(key, Range.closed(0L, size - 1))
                .collectList()
                .flatMapMany(list -> reactiveRedisTemplate.delete(key)
                    .thenMany(Flux.range(0, list.size())
                        .flatMap(i -> {
                            TypedTuple<String> tuple = list.get(i);
                            String value = tuple.getValue() + ":" + (i + 1);
                            return reactiveRedisTemplate.opsForZSet()
                                .add(key, value, tuple.getScore());
                        })
                    )
                )
            ).then();
    }

    private Map<String, Long> countCommitsByLogin(Set<GithubContributor> contributors,
        List<Map> commitList) {

        return contributors.stream()
            .collect(Collectors.toMap(
                GithubContributor::login,
                contributor -> commitList.stream()
                    .filter(commit -> isCommitByAuthor(commit, contributor.login()))
                    .count()
            ));
    }

    private boolean isCommitByAuthor(Map commit, String login) {

        return Optional.ofNullable(commit.get("author"))
            .filter(a -> a instanceof Map)
            .map(a -> (String) ((Map<?, ?>) a).get("login"))
            .map(login::equalsIgnoreCase)
            .orElse(false);
    }

    private Flux<Map> getAllCommits(String owner, String repo, int page) {

        return githubWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/repos/{owner}/{repo}/commits")
                .queryParam("per_page", PER_PAGE)
                .queryParam("page", page)
                .build(owner, repo))
            .retrieve()
            .bodyToFlux(Map.class)
            .collectList()
            .flatMapMany(list -> list.size() < PER_PAGE
                ? Flux.fromIterable(list)
                : Flux.fromIterable(list).concatWith(getAllCommits(owner, repo, page + 1)));
    }

    private Mono<Long> getPullRequestCount(String owner, String repo, String username) {

        return queryGithubIssueCount(owner, repo, username, "type:pr");
    }

    private Mono<Long> getMergedPullRequestCount(String owner, String repo, String username) {

        return queryGithubIssueCount(owner, repo, username, "type:pr+is:merged");
    }

    private Mono<Long> getIssueCount(String owner, String repo, String username) {

        return queryGithubIssueCount(owner, repo, username, "type:issue");
    }

    private Mono<Long> queryGithubIssueCount(String owner, String repo, String username,
        String typeQuery) {
        String query = String.format("repo:%s/%s+%s+author:%s", owner, repo, typeQuery, username);

        return githubWebClient.get()
            .uri(uriBuilder -> uriBuilder.path("/search/issues").queryParam("q", query).build())
            .retrieve()
            .bodyToMono(Map.class)
            .map(result -> ((Number) result.get("total_count")).longValue());
    }

    private String buildRankingKey(String repo) {

        return RANKING_KEY_PREFIX + repo;
    }
}
