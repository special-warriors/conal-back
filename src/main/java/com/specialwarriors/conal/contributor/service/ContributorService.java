package com.specialwarriors.conal.contributor.service;

import com.specialwarriors.conal.contributor.domain.Contributor;
import com.specialwarriors.conal.contributor.repository.ContributorRepository;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContributorService {

    private final String VOTE_SUBJECT = "[Conal] 주간 투표 요청";
    private final String VOTE_MESSAGE = "기여자님의 주간 활동에 대한 투표를 부탁드립니다.";

    private final ContributorRepository contributorRepository;
    private final ContributorQuery contributorQuery;
    private final MailClient mailClient;

    @Scheduled(cron = "0 0 9 * * MON")
    public void sendMail() { //n+1문제 해결 필요
        List<Contributor> contributors = contributorQuery.findAll();
        for (Contributor contributor : contributors) {
            GithubRepo githubRepo = contributor.getGithubRepo();
            if (!githubRepo.getNotificationAgreement().isAgree()) {
                continue;
            }
            String url = githubRepo.getUrl();
            mailClient.sendAsync(contributor.getEmail(), VOTE_SUBJECT,
                VOTE_MESSAGE + "\nURL: " + url);
            ;
        }
    }

}
