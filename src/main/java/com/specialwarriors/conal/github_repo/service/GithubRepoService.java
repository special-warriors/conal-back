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
import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import com.specialwarriors.conal.notification.enums.NotificationType;
import com.specialwarriors.conal.notification.repository.NotificationAgreementRepository;
import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.service.UserQuery;
import com.specialwarriors.conal.util.UrlUtil;
import java.util.List;
import java.util.Set;
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

    private static final int PAGE_SIZE = 7;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile(
        "^(https://)?(www\\.)?github\\.com/[^/\\s]+/[^/\\s]+/?$",
        Pattern.CASE_INSENSITIVE
    );

    private final GithubRepoRepository githubRepoRepository;
    private final ContributorRepository contributorRepository;
    private final NotificationAgreementRepository notificationAgreementRepository;

    private final UserQuery userQuery;
    private final GithubRepoQuery githubRepoQuery;
    private final GithubRepoMapper githubRepoMapper;

    @Transactional
    public GithubRepoCreateResponse createGithubRepo(Long userId, GithubRepoCreateRequest request) {
        validateCreateRequest(request);

        User user = userQuery.findById(userId);
        List<Contributor> contributors = createAndSaveContributors(request.emails());
        List<NotificationAgreement> agreements = createAndAttachNotifications();

        GithubRepo githubRepo = githubRepoMapper.toGithubRepo(request);
        githubRepo.setUser(user);
        githubRepo.addContributors(contributors);
        githubRepo.setNotificationAgreement(agreements);
        githubRepo = githubRepoRepository.save(githubRepo);

        String[] ownerAndRepo = UrlUtil.urlToOwnerAndReponame(githubRepo.getUrl());

        return githubRepoMapper.toGithubRepoCreateResponse(ownerAndRepo[0], ownerAndRepo[1]);
    }

    private void validateCreateRequest(GithubRepoCreateRequest request) {
        if (!request.name().isEmpty()) {
            throw new GeneralException(GithubRepoException.NOT_FOUND_GITHUBREPO_NAME);
        }
        if (!GITHUB_URL_PATTERN.matcher(request.url()).matches()) {
            throw new GeneralException(GithubRepoException.INVALID_GITHUBREPO_URL);
        }
        if (request.emails().isEmpty()) {
            throw new GeneralException(GithubRepoException.NOT_FOUND_GITHUBREPO_EMAIL);
        }
        if (request.emails().size() > 5) {
            throw new GeneralException(GithubRepoException.EXCEED_GITHUBREPO_EMAIL);
        }
        for (String email : request.emails()) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new GeneralException(GithubRepoException.INVALID_GITHUBREPO_EMAIL);
            }
        }
        if (request.endDate() == null) {
            throw new GeneralException(GithubRepoException.INVALID_GITHUBREPO_DURATION);
        }
    }

    private List<Contributor> createAndSaveContributors(Set<String> emails) {

        List<Contributor> contributors = emails.stream()
            .map(Contributor::new).toList();

        return (List<Contributor>) contributorRepository.saveAll(contributors);
    }

    private List<NotificationAgreement> createAndAttachNotifications() {
        return notificationAgreementRepository.saveAll(
            List.of(
                new NotificationAgreement(NotificationType.VOTE),
                new NotificationAgreement(NotificationType.CONTRIBUTION)
            )
        );
    }

    @Transactional(readOnly = true)
    public GithubRepoGetResponse getGithubRepoInfo(Long userId, Long repoId) {
        GithubRepo githubRepo = githubRepoQuery.findByUserIdAndRepositoryId(userId, repoId);
        String[] ownerAndRepo = UrlUtil.urlToOwnerAndReponame(githubRepo.getUrl());

        return githubRepoMapper.toGithubRepoGetResponse(githubRepo, ownerAndRepo[0],
            ownerAndRepo[1], userId);
    }

    @Transactional(readOnly = true)
    public GithubRepoPageResponse getGithubRepoInfos(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<GithubRepo> resultPage = githubRepoRepository.findGithubRepoPages(userId,
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
