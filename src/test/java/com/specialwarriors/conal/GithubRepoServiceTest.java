package com.specialwarriors.conal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.specialwarriors.conal.contributor.domain.Contributor;
import com.specialwarriors.conal.contributor.repository.ContributorRepository;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.dto.GithubRepoMapper;
import com.specialwarriors.conal.github_repo.dto.request.GithubRepoCreateRequest;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoCreateResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoGetResponse;
import com.specialwarriors.conal.github_repo.repository.GithubRepoRepository;
import com.specialwarriors.conal.github_repo.service.GithubRepoQuery;
import com.specialwarriors.conal.github_repo.service.GithubRepoService;
import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import com.specialwarriors.conal.notification.enums.NotificationType;
import com.specialwarriors.conal.notification.repository.NotificationAgreementRepository;
import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.service.UserQuery;
import com.specialwarriors.conal.util.UrlUtil;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GithubRepoServiceTest {

    @InjectMocks
    private GithubRepoService githubRepoService;

    @Mock
    private NotificationAgreementRepository notificationAgreementRepository;

    @Mock
    private GithubRepoRepository githubRepoRepository;

    @Mock
    private ContributorRepository contributorRepository;

    @Mock
    private GithubRepoQuery githubRepoQuery;

    @Mock
    private UserQuery userQuery;

    @Mock
    private GithubRepoMapper githubRepoMapper;

    @Mock
    private GithubRepo mockRepo;

    @Mock
    private List<Contributor> mockContributorList;

    @Mock
    private User mockUser;

    @Mock
    private List<NotificationAgreement> mockNotificationAgreements;

    @BeforeEach
    void setUp() {
        mockRepo = new GithubRepo("test", "https://github.com/owner/reponame", LocalDate.now());
        mockUser = new User(1, "testUser", "testurl");
        mockContributorList = List.of(new Contributor("test@gmail.com"),
            new Contributor("test2@gmail.com"));
        mockNotificationAgreements = List.of(new NotificationAgreement(NotificationType.VOTE),
            new NotificationAgreement(NotificationType.CONTRIBUTION));

        mockRepo.setUser(mockUser);
        mockRepo.addContributors(mockContributorList);
        mockRepo.assignRepoIdToNotificationAgreements(mockNotificationAgreements);

        String[] ownerAndRepo = UrlUtil.urlToOwnerAndReponame(mockRepo.getUrl());
        given(githubRepoMapper.toGithubRepoGetResponse(mockRepo, ownerAndRepo[0],
            ownerAndRepo[1], 1L)).willReturn(
            new GithubRepoGetResponse(
                mockRepo.getUser().getId(),
                mockRepo.getId(),
                mockRepo.getName(),
                mockRepo.getUrl(),
                mockRepo.getEndDate(),
                ownerAndRepo[0],
                ownerAndRepo[1]
            )
        );
    }

    @Test
    @DisplayName("레포를 생성할 수 있다.")
    void createRepo() {
        //given
        given(githubRepoService.createGithubRepo(1L,
            new GithubRepoCreateRequest("repoName", "https://github.com/owner/reponame",
                LocalDate.now(), Set.of("test@test.com"))));

        //when
        GithubRepoCreateResponse result = githubRepoService.createGithubRepo(1L,
            new GithubRepoCreateRequest("repoName", "https://github.com/owner/reponame",
                LocalDate.now(), Set.of("test@test.com")));

        assertThat(result.repo()).isEqualTo("repoName");
        assertThat(result.owner()).isEqualTo("owner");

    }

    @Test
    @DisplayName("레포 아이디와 유저 아이디로 깃 레포를 검색할 수 있다.")
    void findRepoByUserIdAndRepositoryId() {
        //given
        given(githubRepoQuery.findByUserIdAndRepositoryId(1L, 1L)).willReturn(mockRepo);

        //when
        GithubRepoGetResponse result = githubRepoService.getGithubRepoInfo(1L, 1L);

        // then
        assertThat(result.name()).isEqualTo(mockRepo.getName());
        assertThat(result.userId()).isEqualTo(mockRepo.getUser().getId());
        assertThat(result.url()).isEqualTo(mockRepo.getUrl());
    }

}
