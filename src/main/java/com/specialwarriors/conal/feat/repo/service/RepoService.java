package com.specialwarriors.conal.feat.repo.service;

import com.specialwarriors.conal.feat.contributor.domain.Contributor;
import com.specialwarriors.conal.feat.contributor.repository.ContributorRepository;
import com.specialwarriors.conal.feat.notificationagreement.domain.NotificationAgreement;
import com.specialwarriors.conal.feat.notificationagreement.repository.NotificationAgreementRepository;
import com.specialwarriors.conal.feat.repo.domain.Repo;
import com.specialwarriors.conal.feat.repo.dto.RepoMapper;
import com.specialwarriors.conal.feat.repo.dto.request.RepoCreateRequest;
import com.specialwarriors.conal.feat.repo.dto.response.RepoCreateResponse;
import com.specialwarriors.conal.feat.repo.repository.RepoRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RepoService {

    private final RepoRepository repoRepository;
    private final ContributorRepository contributorRepository;
    private final NotificationAgreementRepository notificationAgreementRepository;
    private final RepoMapper repoMapper;

    @Transactional
    public RepoCreateResponse createRepo(RepoCreateRequest request) {
        List<Contributor> contributors = createAndSaveContributors(request.emails());
        NotificationAgreement notificationAgreement = createAndSaveNotificationAgreement();

        Repo repo = repoRepository.save(
            repoMapper.toRepo(request));
        repo.addContributors(contributors);
        repo.setNotificationAgreement(notificationAgreement);

        return repoMapper.toRepoCreateResponse(repo);
    }

    private List<Contributor> createAndSaveContributors(Set<String> emails) {
        List<Contributor> contributors = emails.stream()
            .map(Contributor::of).toList();
        return (List<Contributor>) contributorRepository.saveAll(contributors);
    }

    private NotificationAgreement createAndSaveNotificationAgreement() {
        NotificationAgreement notificationAgreement = NotificationAgreement.of(false);
        return notificationAgreementRepository.save(notificationAgreement);
    }


}
