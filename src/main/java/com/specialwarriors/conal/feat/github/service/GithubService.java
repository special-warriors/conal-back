package com.specialwarriors.conal.feat.github.service;

import com.specialwarriors.conal.feat.github.dto.GithubContributor;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private static final int PER_PAGE = 100; // 100개씩 호출할 수 있어서 paging 사용
    private final WebClient githubWebClient;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final Set<GithubContributor> contributors = new LinkedHashSet<>();


    /**
     * 모든 rank를 반영
     */
    public Mono<Void> updateAllRanks(String owner, String repo) {

        return getContributorList(owner, repo)
            .then(updateTotalContributionRanking(owner, repo));
    }

    /**
     * 점수반영하는 것 -> 계산
     */
    public Mono<Void> updateTotalContributionRanking(String owner, String repo) {

        return getAllCommits(owner, repo, 1, PER_PAGE)
            .collectList()
            .flatMap(commitList -> {
                Map<String, Long> commitCountMap = countCommitsByLogin(contributors, commitList);

                return Flux.fromIterable(contributors)
                    .flatMap(contributor -> {
                        String username = contributor.getLogin();
                        long commitCount = commitCountMap.getOrDefault(username, 0L);

                        Mono<Long> prCountMono = getPullRequestCount(owner, repo, username);
                        Mono<Long> mprCountMono = getMergedPullRequestCount(owner, repo, username);
                        Mono<Long> issueCountMono = getIssueCount(owner, repo, username);

                        return Mono.zip(prCountMono, mprCountMono, issueCountMono)
                            .flatMap(tuple -> {
                                long pr = tuple.getT1();
                                long mpr = tuple.getT2();
                                long issue = tuple.getT3();

                                long totalScore =
                                    commitCount + pr + mpr + issue; // 여기서 기여 정도 바꿀 수 있어요

                                log.info("{} commit={}, pr={}, mpr={}, issue={} total={}",
                                    username, commitCount, pr, mpr, issue, totalScore);

                                return saveToRedisRanking("ranking:" + repo, username, totalScore);
                            });
                    })
                    .then()
                    .then(saveRankingPositionsToRedis(repo));
            });
    }

    /**
     * contributor에 contribution 기록이 있으면 가져옴
     */
    public Mono<Void> getContributorList(String owner, String repo) {
        Mono<List<GithubContributor>> fetchContributors = githubWebClient.get()
            .uri(uriBuilder -> uriBuilder.path("/repos/{owner}/{repo}/contributors")
                .build(owner, repo))
            .retrieve()
            .bodyToFlux(GithubContributor.class)
            .collectList();

        Mono<Map<String, String>> loginToEmailMap = getAllCommits(owner, repo, 1, PER_PAGE)
            .collectList()
            .map(this::extractLoginToEmailMap);

        return Mono.zip(fetchContributors, loginToEmailMap)
            .doOnNext(tuple -> {
                List<GithubContributor> fetchedContributors = tuple.getT1();
                Map<String, String> emailMap = tuple.getT2();

                contributors.clear();
                for (GithubContributor dto : fetchedContributors) {
                    String login = dto.getLogin();
                    dto.setEmail(emailMap.getOrDefault(login, null));
                    contributors.add(dto);
                }

                log.info("{}명 : {}", contributors.size(),
                    contributors.stream().map(GithubContributor::getLogin).toList());
            })
            .then();
    }

    /**
     * 쓸데없는 확인 후 mapping → login과 email 맵핑
     */
    private Map<String, String> extractLoginToEmailMap(List<Map> commitList) {

        return commitList.stream()
            .map(commit -> {
                Optional<String> loginOpt = Optional.ofNullable(commit.get("author"))
                    .filter(m -> m instanceof Map)
                    .map(m -> (Map<?, ?>) m)
                    .map(m -> (String) m.get("login"));

                Optional<String> emailOpt = Optional.ofNullable(commit.get("commit"))
                    .filter(m -> m instanceof Map)
                    .map(m -> ((Map<?, ?>) m).get("author"))
                    .filter(m -> m instanceof Map)
                    .map(m -> (String) ((Map<?, ?>) m).get("email"));

                return loginOpt.flatMap(login ->
                    emailOpt.map(email -> Map.entry(login, email))
                ).orElse(null);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (existing, replacement) -> existing));
    }

    /**
     * redis에 저장 후 순위를 반영해 봐요
     */

    /**
     * redis에 저장하는 것
     */
    private Mono<Void> saveToRedisRanking(String key, String username, long score) {

        return reactiveRedisTemplate.opsForZSet()
            .add(key, username, score)
            .flatMap(result -> {
                if (Boolean.TRUE.equals(result)) {
                    log.info("success: {} {} = {}", key, username, score);
                } else {
                    log.info("fail {} {}", key, username);
                }
                return Mono.empty();
            });
    }

    /**
     * 저장 후 반영 -> 순위를 반영도 함 :1이런 형태로 ? 바꾸고 싶으면 말하세요 순위 반영 하는것
     */
    private Mono<Void> saveRankingPositionsToRedis(String repo) {
        String key = "ranking:" + repo;

        return reactiveRedisTemplate.opsForZSet().size(key)
            .flatMapMany(size -> reactiveRedisTemplate.opsForZSet()
                .reverseRangeWithScores(key, Range.closed(0L, size - 1))
                .collectList()
                .flatMapMany(list -> reactiveRedisTemplate.delete(key)
                    .thenMany(Flux.range(0, list.size())
                        .flatMap(i -> {
                            TypedTuple<String> tuple = list.get(i);
                            String username = tuple.getValue();
                            long rank = i + 1;
                            double score = tuple.getScore();

                            String valueWithRank = username + ":" + rank;

                            return reactiveRedisTemplate.opsForZSet()
                                .add(key, valueWithRank, score)
                                .doOnSuccess(result -> log.info("ZSet save: {} (score={}, rank={})",
                                    valueWithRank, score, rank));
                        }))
                ))
            .then();
    }

    /**
     * commit count 를 계산을 해봐요
     */

    /**
     * commit  -> commit이 이상하다는 의견이 있어서 login(username)과 email(git) id를 합쳐서 했는데 상관없었어요
     */
    private Map<String, Long> countCommitsByLogin(Set<GithubContributor> contributors,
        List<Map> commitList) {

        return contributors.stream()
            .collect(Collectors.toMap(GithubContributor::getLogin, contributor -> {
                String login = contributor.getLogin();
                String email = contributor.getEmail();

                long count = commitList.stream()
                    .filter(commit -> {
                        boolean matchByLogin = Optional.ofNullable(commit.get("author"))
                            .filter(authorObj -> authorObj instanceof Map)
                            .map(authorMap -> (String) ((Map<?, ?>) authorMap).get("login"))
                            .map(loginValue -> loginValue.equalsIgnoreCase(login))
                            .orElse(false);

                        Map<?, ?> commitMap = Optional.ofNullable(commit.get("commit"))
                            .filter(obj -> obj instanceof Map)
                            .map(obj -> (Map<?, ?>) obj)
                            .orElse(Map.of());

                        Map<?, ?> commitAuthorMap = Optional.ofNullable(commitMap.get("author"))
                            .filter(obj -> obj instanceof Map)
                            .map(obj -> (Map<?, ?>) obj)
                            .orElse(Map.of());

                        String commitEmail = (String) commitAuthorMap.get("email");
                        String commitName = (String) commitAuthorMap.get("name");

                        boolean matchByEmail = email != null && email.equalsIgnoreCase(commitEmail);
                        boolean matchByName = commitName != null &&
                            commitName.toLowerCase().replace(" ", "")
                                .contains(login.toLowerCase().replace("-", ""));

                        return matchByLogin || matchByEmail || matchByName;
                    })
                    .count();

                return count;
            }));
    }

    /**
     * api로 값들을 갖고 오는 곳이에요 성능이 개느려요 저희 레포 분석하는데 총 4초 정도 걸리는 것 같아요
     */


    /**
     * 페이징을 통해서 commit 수를 읽는 방법 -> commit은 아무래도 많기 때문에?
     */
    private Flux<Map> getAllCommits(String owner, String repo, int page, int perPage) {

        return githubWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/repos/{owner}/{repo}/commits")
                .queryParam("per_page", perPage)
                .queryParam("page", page)
                .build(owner, repo))
            .retrieve()
            .bodyToFlux(Map.class)
            .collectList()
            .flatMapMany(commitList -> {
                if (commitList.size() < perPage) {

                    return Flux.fromIterable(commitList);
                } else {

                    return Flux.fromIterable(commitList)
                        .concatWith(getAllCommits(owner, repo, page + 1, perPage));
                }
            });
    }


    /**
     * pr을 가져오는 갯수
     */
    private Mono<Long> getPullRequestCount(String owner, String repo, String username) {

        return githubWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/search/issues")
                .queryParam("q", "repo:" + owner + "/" + repo + "+type:pr+author:" + username)
                .build())
            .retrieve()
            .bodyToMono(Map.class)
            .map(result -> ((Number) result.get("total_count")).longValue());
    }

    /**
     * mpr을 가져오는 갯수
     */
    private Mono<Long> getMergedPullRequestCount(String owner, String repo, String username) {

        return githubWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/search/issues")
                .queryParam("q",
                    "repo:" + owner + "/" + repo + "+type:pr+author:" + username + "+is:merged")
                .build())
            .retrieve()
            .bodyToMono(Map.class)
            .map(result -> ((Number) result.get("total_count")).longValue());
    }

    /**
     * Issue를 가져오는 갯수 -> 생성 개수만
     */
    private Mono<Long> getIssueCount(String owner, String repo, String username) {

        return githubWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/search/issues")
                .queryParam("q", "repo:" + owner + "/" + repo + "+type:issue+author:" + username)
                .build())
            .retrieve()
            .bodyToMono(Map.class)
            .map(result -> ((Number) result.get("total_count")).longValue());
    }
}
