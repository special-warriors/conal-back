package com.specialwarriors.conal.github.service;

import com.specialwarriors.conal.github.dto.GitHubContributor;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubService {

    private static final int PER_PAGE = 100;
    private static final Duration TTL = Duration.ofMinutes(35);
    private static final String DETAIL_KEY_PREFIX = "detail:";
    private static final String CONTRIBUTORS_KEY_PREFIX = "contributors:";

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final WebClient githubWebClient;

    public Mono<Void> updateRepoContribution(String owner, String repo) {

        return getContributors(owner, repo)
                .flatMapMany(contributors -> {
                    String contributorKey = buildContributorsKey(owner, repo);
                    List<String> logins = contributors.stream()
                            .map(GitHubContributor::login)
                            .collect(Collectors.toList());

                    return reactiveRedisTemplate.delete(contributorKey)
                            .thenMany(Flux.fromIterable(logins))
                            .flatMap(login -> reactiveRedisTemplate.opsForList()
                                    .rightPush(contributorKey, login))
                            .then(reactiveRedisTemplate.expire(contributorKey, TTL))
                            .thenMany(Flux.fromIterable(contributors));
                })
                .flatMap(contributor -> updateContributorScore(owner, repo, contributor))
                .then();
    }

    private Mono<List<GitHubContributor>> getContributors(String owner, String repo) {

        return githubWebClient.get()
                .uri("/repos/{owner}/{repo}/contributors", owner, repo)
                .retrieve()
                .bodyToFlux(GitHubContributor.class)
                .collectList();
    }

    private Mono<Void> updateContributorScore(String owner, String repo,
            GitHubContributor contributor) {

        String login = contributor.login();

        return Mono.zip(
                getCommitCount(owner, repo, login),
                getPullRequestCount(owner, repo, login),
                getMergedPullRequestCount(owner, repo, login),
                getIssueCount(owner, repo, login)
        ).flatMap(tuple -> {
            long commit = tuple.getT1();
            long pr = tuple.getT2();
            long mpr = tuple.getT3();
            long issue = tuple.getT4();
            long totalScore = commit + pr + mpr + issue;

            String detailKey = buildDetailKey(owner, repo, login);

            return reactiveRedisTemplate.opsForHash().putAll(detailKey, Map.of(
                            "commit", String.valueOf(commit),
                            "pr", String.valueOf(pr),
                            "mpr", String.valueOf(mpr),
                            "issue", String.valueOf(issue),
                            "total", String.valueOf(totalScore)
                    ))
                    .then(reactiveRedisTemplate.expire(detailKey, TTL));
        }).then();
    }

    private Mono<Long> getCommitCount(String owner, String repo, String login) {

        return getAllCommits(owner, repo, 1)
                .filter(commit -> {
                    Map author = (Map) commit.get("author");

                    return author != null && login.equalsIgnoreCase((String) author.get("login"));
                })
                .count();
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
                .flatMapMany(list -> list.size() < PER_PAGE ? Flux.fromIterable(list)
                        : Flux.fromIterable(list).concatWith(getAllCommits(owner, repo, page + 1)));
    }

    private Mono<Long> getPullRequestCount(String owner, String repo, String login) {

        return queryGithubIssueCount(owner, repo, login, "type:pr");
    }

    private Mono<Long> getMergedPullRequestCount(String owner, String repo, String login) {

        return queryGithubIssueCount(owner, repo, login, "type:pr+is:merged");
    }

    private Mono<Long> getIssueCount(String owner, String repo, String login) {

        return queryGithubIssueCount(owner, repo, login, "type:issue");
    }

    private Mono<Long> queryGithubIssueCount(String owner, String repo, String login,
            String queryType) {
        String query = String.format("repo:%s/%s+%s+author:%s", owner, repo, queryType, login);

        return githubWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search/issues").queryParam("q", query).build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> ((Number) map.get("total_count")).longValue());
    }

    private String buildDetailKey(String owner, String repo, String login) {

        return DETAIL_KEY_PREFIX + owner + ":" + repo + ":" + login;
    }

    private String buildContributorsKey(String owner, String repo) {

        return CONTRIBUTORS_KEY_PREFIX + owner + ":" + repo;
    }

    /**
     * owner/repo에 속한 모든 contributor 목록 가져오기
     */
    public Mono<List<String>> getContributorsFromRedis(String owner, String repo) {
        String key = "contributors:" + owner + ":" + repo;

        return reactiveRedisTemplate.opsForList()
                .range(key, 0, -1)
                .collectList();
    }

    /**
     * 특정 contributor에 대한 상세 점수 정보 가져오기
     */
    public Mono<Map<String, String>> getContributorDetailFromRedis(String owner, String repo,
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
