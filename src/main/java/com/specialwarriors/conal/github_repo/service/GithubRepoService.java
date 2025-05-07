package com.specialwarriors.conal.github_repo.service;

import com.specialwarriors.conal.contributor.domain.Contributor;
import com.specialwarriors.conal.contributor.repository.ContributorRepository;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.dto.GithubRepoMapper;
import com.specialwarriors.conal.github_repo.dto.request.GithubRepoCreateRequest;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoCreateResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoDeleteResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoGetResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoPageResponse;
import com.specialwarriors.conal.github_repo.repository.GithubRepoRepository;
import com.specialwarriors.conal.github_repo.repository.GithubRepoRepositoryCustom;
import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import com.specialwarriors.conal.notification.enums.NotificationType;
import com.specialwarriors.conal.notification.repository.NotificationAgreementRepository;
import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.service.UserQuery;
import java.util.List;
import java.util.Set;
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

        List<Contributor> contributors = createAndSaveContributors(request.emails());
        NotificationAgreement notificationAgreement = createAndSaveNotificationAgreement();

        GithubRepo githubRepo = githubRepoRepository.save(
            githubRepoMapper.toGithubRepo(request));

        githubRepo.addContributors(contributors);
        githubRepo.setNotificationAgreement(notificationAgreement);
        githubRepo.setUser(user);

        return githubRepoMapper.toGithubRepoCreateResponse(githubRepo);
    }

    private List<Contributor> createAndSaveContributors(Set<String> emails) {
        List<Contributor> contributors = emails.stream()
            .map(Contributor::of).toList();
        return (List<Contributor>) contributorRepository.saveAll(contributors);
    }

    private NotificationAgreement createAndSaveNotificationAgreement() {
        NotificationAgreement notificationAgreement = NotificationAgreement.of(
            NotificationType.VOTE);
        return notificationAgreementRepository.save(notificationAgreement);
    }

    @Transactional(readOnly = true)
    public GithubRepoGetResponse getGithubRepo(Long userId, Long repoId) {

        GithubRepo githubRepo = githubRepoQuery.findByUserIdAndRepositoryId(userId, repoId);

        return githubRepoMapper.toGithubRepoGetResponse(githubRepo);
    }


    @Transactional(readOnly = true)
    public GithubRepoPageResponse getGithubRepos(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<GithubRepo> resultPage = githubRepoRepositoryCustom.findGithubRepoPages(userId,
            pageable);

        return githubRepoMapper.toGithubRepoPageResponse(resultPage);
    }

    @Transactional
    public GithubRepoDeleteResponse deleteRepo(Long userId, Long repositoryId) {
        GithubRepo repo = githubRepoQuery.findByUserIdAndRepositoryId(userId, repositoryId);

        githubRepoRepository.delete(repo);

        return githubRepoMapper.toGithubDeleteRepoResponse();
    }
}
