package com.specialwarriors.conal.github_repo.service;

import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.contributor.domain.Contributor;
import com.specialwarriors.conal.contributor.repository.ContributorRepository;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.dto.GithubRepoMapper;
import com.specialwarriors.conal.github_repo.dto.request.GithubRepoCreateRequest;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoCreateResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoDeleteResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoGetResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoPageResponse;
import com.specialwarriors.conal.github_repo.exception.GithubRepoException;
import com.specialwarriors.conal.github_repo.repository.GithubRepoRepository;
import com.specialwarriors.conal.github_repo.repository.GithubRepoRepositoryCustom;
import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import com.specialwarriors.conal.notification.enums.NotificationType;
import com.specialwarriors.conal.notification.repository.NotificationAgreementRepository;
import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.service.UserQuery;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GithubRepoService {

    private final int PAGE_SIZE = 7;

    private final GithubRepoRepository githubRepoRepository;
    private final ContributorRepository contributorRepository;
    private final NotificationAgreementRepository notificationAgreementRepository;
    private final GithubRepoRepositoryCustom githubRepoRepositoryCustom;

    private final UserQuery userQuery;
    private final GithubRepoQuery githubRepoQuery;
    private final GithubRepoMapper githubRepoMapper;

    @Transactional
    public GithubRepoCreateResponse createGithubRepo(Long userId, GithubRepoCreateRequest request) {
        User user = userQuery.findById(userId);
        validateGitHubUrl(request.url());

        List<Contributor> contributors = createAndSaveContributors(request.emails());

        GithubRepo githubRepo = githubRepoMapper.toGithubRepo(request);
        githubRepo.setUser(user);
        githubRepo.addContributors(contributors);

        githubRepo = githubRepoRepository.save(githubRepo);

        NotificationAgreement agreement = notificationAgreementRepository.save(
            new NotificationAgreement(NotificationType.VOTE));
        agreement.setGitHubRepo(githubRepo);

        githubRepo.setNotificationAgreement(agreement);

        return githubRepoMapper.toGithubRepoCreateResponse(githubRepo);
    }

    private void validateGitHubUrl(String url) {
        // GitHub 저장소 URL 형식 검증: https://github.com/{owner}/{repo}
        String regex = "^https://github\\.com/[^/]+/[^/]+$";
        if (!url.matches(regex)) {
            throw new GeneralException(GithubRepoException.INVALID_URL);
        }
    }

    private List<Contributor> createAndSaveContributors(Set<String> emails) {
        if (emails.isEmpty()) {
            throw new GeneralException(GithubRepoException.NOT_FOUND_GITHUBEMAIL);
        }

        List<Contributor> contributors = emails.stream()
            .map(Contributor::new).toList();
        return (List<Contributor>) contributorRepository.saveAll(contributors);
    }

    @Transactional(readOnly = true)
    public GithubRepoGetResponse getGithubRepoInfo(Long userId, Long repoId) {

        GithubRepo githubRepo = githubRepoQuery.findByUserIdAndRepositoryId(userId, repoId);

        Pattern pattern = Pattern.compile("https://github\\.com/([^/]+)/([^/]+)");
        Matcher matcher = pattern.matcher(githubRepo.getUrl());
        if (matcher.find()) {
            String owner = matcher.group(1);
            String repo = matcher.group(2);

            return githubRepoMapper.toGithubRepoGetResponse(githubRepo, owner, repo);
        } else {
            throw new GeneralException(GithubRepoException.NOT_FOUND_GITHUBREPO);
        }
    }

    @Transactional(readOnly = true)
    public GithubRepoPageResponse getGithubRepoInfos(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<GithubRepo> resultPage = githubRepoRepositoryCustom.findGithubRepoPages(userId,
            pageable);

        return githubRepoMapper.toGithubRepoPageResponse(resultPage, userId);
    }

    @Transactional
    public GithubRepoDeleteResponse deleteRepo(Long userId, Long repositoryId) {
        GithubRepo repo = githubRepoQuery.findByUserIdAndRepositoryId(userId, repositoryId);
        contributorRepository.deleteAllByGithubRepo(repo);
        notificationAgreementRepository.deleteByGithubRepo(repo);
        githubRepoRepository.delete(repo);

        return githubRepoMapper.toGithubDeleteRepoResponse();
    }
}
