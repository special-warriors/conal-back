package com.specialwarriors.conal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.contributor.domain.Contributor;
import com.specialwarriors.conal.contributor.repository.ContributorRepository;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.dto.GithubRepoMapper;
import com.specialwarriors.conal.github_repo.dto.request.GithubRepoCreateRequest;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoCreateResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoGetResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoPageResponse;
import com.specialwarriors.conal.github_repo.exception.GithubRepoException;
import com.specialwarriors.conal.github_repo.repository.GithubRepoRepository;
import com.specialwarriors.conal.github_repo.service.GithubRepoQuery;
import com.specialwarriors.conal.github_repo.service.GithubRepoService;
import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import com.specialwarriors.conal.notification.enums.NotificationType;
import com.specialwarriors.conal.notification.repository.NotificationAgreementRepository;
import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.exception.UserException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

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

    private GithubRepo mockRepo;
    private User mockUser;
    private List<Contributor> mockContributorList;
    private List<NotificationAgreement> mockNotificationAgreements;

    @BeforeEach
    void setUp() {
        mockUser = new User(1, "testUser", "testurl");
        mockRepo = new GithubRepo("test", "https://github.com/owner/reponame", LocalDate.now());
        ReflectionTestUtils.setField(mockRepo, "id", 1L);
        ReflectionTestUtils.setField(mockUser, "id", 1L);
        mockRepo.setUser(mockUser);

        mockContributorList = List.of(
                new Contributor("test@gmail.com"),
                new Contributor("test2@gmail.com")
        );

        mockNotificationAgreements = List.of(
                new NotificationAgreement(NotificationType.VOTE),
                new NotificationAgreement(NotificationType.CONTRIBUTION)
        );

        mockRepo.addContributors(mockContributorList);
        mockRepo.assignRepoIdToNotificationAgreements(mockNotificationAgreements);
    }

    @Test
    @DisplayName("레포를 생성할 수 있다.")
    void createRepo() {
        // given
        final String name = "repoName";
        final String url = "https://github.com/owner/reponame";
        final LocalDate endDate = LocalDate.now();

        GithubRepoCreateRequest request = new GithubRepoCreateRequest(
                name,
                url,
                endDate,
                Set.of("test@test.com")
        );

        GithubRepo githubRepo = new GithubRepo(name, url, endDate);
        GithubRepoCreateResponse response = new GithubRepoCreateResponse(
                "owner",
                name
        );

        given(userQuery.findById(1L)).willReturn(mockUser);
        given(githubRepoRepository.save(any(GithubRepo.class))).willReturn(mockRepo);
        given(githubRepoMapper.toGithubRepo(request)).willReturn(githubRepo);
        given(contributorRepository.saveAll(anyList())).willReturn(mockContributorList);
        given(githubRepoMapper.toGithubRepoCreateResponse(
                any(String.class), any(String.class))
        ).willReturn(response);

        // when
        GithubRepoCreateResponse result = githubRepoService.createGithubRepo(1L, request);

        // then
        assertThat(result.repo()).isEqualTo(name);
        assertThat(result.owner()).isEqualTo("owner");
    }

    @Test
    @DisplayName("레포 아이디와 유저 아이디로 깃 레포를 조회할 수 있다.")
    void findRepoByUserIdAndRepositoryId() {
        // given
        given(githubRepoQuery.findByUserIdAndRepositoryId(1L, 1L)).willReturn(mockRepo);

        String[] ownerAndRepo = UrlUtil.urlToOwnerAndReponame(mockRepo.getUrl());
        given(githubRepoMapper.toGithubRepoGetResponse(mockRepo, ownerAndRepo[0], ownerAndRepo[1],
                1L)).willReturn(
                new GithubRepoGetResponse(
                        mockUser.getId(),
                        mockRepo.getId(),
                        mockRepo.getName(),
                        mockRepo.getUrl(),
                        mockRepo.getEndDate(),
                        ownerAndRepo[0],
                        ownerAndRepo[1]
                )
        );

        // when
        GithubRepoGetResponse result = githubRepoService.getGithubRepoInfo(1L, 1L);

        // then
        assertThat(result.name()).isEqualTo(mockRepo.getName());
        assertThat(result.userId()).isEqualTo(mockRepo.getUser().getId());
        assertThat(result.url()).isEqualTo(mockRepo.getUrl());
    }

    @Test
    @DisplayName("레포를 삭제할 수 있다")
    void deleteRepo() {
        // given
        given(githubRepoQuery.findByUserIdAndRepositoryId(1L, 1L)).willReturn(mockRepo);

        // when
        githubRepoService.deleteRepo(1L, 1L);

        // then
        verify(contributorRepository).deleteAllByGithubRepo(mockRepo);
        verify(notificationAgreementRepository).deleteByGithubRepoId(mockRepo.getId());
        verify(githubRepoRepository).delete(mockRepo);
    }

    @Test
    @DisplayName("레포를 생성할 때 레포 이름이 없으면 예외를 던진다")
    void throwsExceptionWhenCreatingRepoWithoutName() {
        // given
        final String name = "";
        final String url = "https://github.com/owner/reponame";
        final LocalDate endDate = LocalDate.now();

        GithubRepoCreateRequest request = new GithubRepoCreateRequest(
                name,
                url,
                endDate,
                Set.of("test@test.com")
        );

        // when & then
        assertThatThrownBy(() -> githubRepoService.createGithubRepo(1L, request))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(GithubRepoException.NOT_FOUND_GITHUBREPO_NAME.getMessage());
    }

    @Test
    @DisplayName("레포를 생성할 때 url형식이 맞지 않으면 예외를 던진다")
    void throwsExceptionWhenCreatingRepoInvalidUrl() {
        // given
        final String name = "repoName";
        final String url = "https://github.com/owner";
        final LocalDate endDate = LocalDate.now();

        GithubRepoCreateRequest request = new GithubRepoCreateRequest(
                name,
                url,
                endDate,
                Set.of("test@test.com")
        );

        // when & then
        assertThatThrownBy(() -> githubRepoService.createGithubRepo(1L, request))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(GithubRepoException.INVALID_GITHUBREPO_URL.getMessage());
    }

    @Test
    @DisplayName("레포를 생성할 때 url이 없으면 예외를 던진다.")
    void throwsExceptionWhenCreatingRepoWithoutOwnerName() {
        // given
        final String name = "repoName";
        final String url = "";
        final LocalDate endDate = LocalDate.now();

        GithubRepoCreateRequest request = new GithubRepoCreateRequest(
                name,
                url,
                endDate,
                Set.of("test@test.com")
        );

        // when & then
        assertThatThrownBy(() -> githubRepoService.createGithubRepo(1L, request))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(GithubRepoException.INVALID_GITHUBREPO_URL.getMessage());
    }

    @Test
    @DisplayName("레포를 생성할 때 팀원 이메일이 없으면 예외를 던진다.")
    void throwsExceptionWhenCreatingRepoWithoutEmail() {
        // given
        final String name = "repoName";
        final String url = "https://github.com/owner/reponame";
        final LocalDate endDate = LocalDate.now();

        GithubRepoCreateRequest request = new GithubRepoCreateRequest(
                name,
                url,
                endDate,
                Set.of("")
        );

        // when & then
        assertThatThrownBy(() -> githubRepoService.createGithubRepo(1L, request))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(GithubRepoException.NOT_FOUND_GITHUBREPO_EMAIL.getMessage());
    }

    @Test
    @DisplayName("레포를 생성할 때 팀원 이메일 5개가 넘으면 예외를 던진다.")
    void throwsExceptionWhenCreatingRepoUpperBoundEmail() {
        // given
        final String name = "repoName";
        final String url = "https://github.com/owner/reponame";
        final LocalDate endDate = LocalDate.now();

        GithubRepoCreateRequest request = new GithubRepoCreateRequest(
                name,
                url,
                endDate,
                Set.of("aaa@aaa.ca", "bbb@bbb.ca", "ccc@ccc.ca", "ddd@ddd.ca", "eee@eee.ca",
                        "fff@fff.ca")
        );

        // when & then
        assertThatThrownBy(() -> githubRepoService.createGithubRepo(1L, request))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(GithubRepoException.EXCEED_GITHUBREPO_EMAIL.getMessage());
    }

    @Test
    @DisplayName("레포를 생성할 때 팀원 종료일이 없으면 예외를 던진다.")
    void throwsExceptionWhenCreatingRepoWithOutDuration() {
        // given
        final String name = "repoName";
        final String url = "https://github.com/owner/reponame";
        final LocalDate endDate = null;

        GithubRepoCreateRequest request = new GithubRepoCreateRequest(
                name,
                url,
                endDate,
                Set.of("test@test.com", "test2@test2.com")
        );

        // when & then
        assertThatThrownBy(() -> githubRepoService.createGithubRepo(1L, request))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(GithubRepoException.INVALID_GITHUBREPO_DURATION.getMessage());
    }


    @Test
    @DisplayName("레포를 조회할 때 userId가 없으면 예외를 던진다")
    void throwsExceptionWhenSearchingRepoWithoutUserId() {
        // given
        given(githubRepoQuery.findByUserIdAndRepositoryId(null, 1L))
                .willThrow(new GeneralException(UserException.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> githubRepoService.getGithubRepoInfo(null, 1L))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(UserException.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("레포를 조회할 때 repoId가 없으면 예외를 던진다")
    void throwsExceptionWhenSearchingRepoWithoutRepoId() {
        // given
        given(githubRepoQuery.findByUserIdAndRepositoryId(1L, null))
                .willThrow(new GeneralException(GithubRepoException.NOT_FOUND_GITHUBREPO));

        // when & then
        assertThatThrownBy(() -> githubRepoService.getGithubRepoInfo(1L, null))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(GithubRepoException.NOT_FOUND_GITHUBREPO.getMessage());
    }

    @Test
    @DisplayName("레포를 조회할 때 github_repo.userId와 userId가 일치하지 않으면 예외를 던진다")
    void throwsExceptionWhenSearchingRepoMissMatchUserId() {
        // given
        given(githubRepoQuery.findByUserIdAndRepositoryId(2L, 1L))
                .willThrow(
                        new GeneralException(GithubRepoException.UNAUTHORIZED_GITHUBREPO_ACCESS));

        // when & then
        assertThatThrownBy(() -> githubRepoService.getGithubRepoInfo(2L, 1L))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(
                        GithubRepoException.UNAUTHORIZED_GITHUBREPO_ACCESS.getMessage());
    }

    @Test
    @DisplayName("레포를 삭제할 때 userId가 없으면 예외를 던진다")
    void throwsExceptionWhenDeletingRepoWithoutUserId() {
        // given
        given(githubRepoQuery.findByUserIdAndRepositoryId(null, 1L))
                .willThrow(new GeneralException(UserException.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> githubRepoService.deleteRepo(null, 1L))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(UserException.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("레포를 삭제할 때 repoId가 없으면 예외를 던진다")
    void throwsExceptionWhenDeletingRepoWithoutRepoId() {
        // given
        given(githubRepoQuery.findByUserIdAndRepositoryId(1L, null))
                .willThrow(new GeneralException(GithubRepoException.NOT_FOUND_GITHUBREPO));

        // when & then
        assertThatThrownBy(() -> githubRepoService.deleteRepo(1L, null))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(GithubRepoException.NOT_FOUND_GITHUBREPO.getMessage());
    }

    @Test
    @DisplayName("레포를 삭제할 때 repo.userId와 userId가 일치하지 않으면 예외를 던진다")
    void throwsExceptionWhenDeletingRepoMissMatchUserId() {
        // given
        given(githubRepoQuery.findByUserIdAndRepositoryId(2L, 1L))
                .willThrow(
                        new GeneralException(GithubRepoException.UNAUTHORIZED_GITHUBREPO_ACCESS));

        // when & then
        assertThatThrownBy(() -> githubRepoService.deleteRepo(2L, 1L))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(
                        GithubRepoException.UNAUTHORIZED_GITHUBREPO_ACCESS.getMessage());
    }


    @Test
    @DisplayName("깃 허브 레포 페이지네이션 조회: 페이지 0")
    void getGithubRepoPageZero() {
        // given
        int page = 0;
        int pageSize = 7;
        Long userId = 1L;
        Pageable pageable = PageRequest.of(page, pageSize);
        List<GithubRepo> repos = List.of(
                new GithubRepo("repo1", "https://github.com/user/repo1", LocalDate.now()),
                new GithubRepo("repo2", "https://github.com/user/repo2", LocalDate.now()),
                new GithubRepo("repo3", "https://github.com/user/repo3", LocalDate.now()),
                new GithubRepo("repo4", "https://github.com/user/repo4", LocalDate.now())
        );

        for (GithubRepo repo : repos) {
            repo.setUser(mockUser);
        }

        Page<GithubRepo> mockRepoPage = new PageImpl<>(repos, pageable, repos.size());

        List<GithubRepoPageResponse.GithubRepoSummary> summaries = repos.stream()
                .map(repo -> new GithubRepoPageResponse.GithubRepoSummary(
                        repo.getUser().getId(),
                        repo.getName(),
                        repo.getUrl(),
                        repo.getEndDate()))
                .toList();

        GithubRepoPageResponse expectedMapperResponse = new GithubRepoPageResponse(
                summaries,
                userId,
                page,
                1,
                repos.size()
        );

        given(githubRepoRepository.findGithubRepoPages(eq(userId), any(Pageable.class)))
                .willReturn(mockRepoPage);

        given(githubRepoMapper.toGithubRepoPageResponse(eq(mockRepoPage), eq(userId)))
                .willReturn(expectedMapperResponse);

        // when
        GithubRepoPageResponse result = githubRepoService.getGithubRepoInfos(userId, page);

        // then
        assertThat(result).isNotNull();
        assertThat(result.repositoryId()).hasSize(4);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.repositoryId().get(0).name()).isEqualTo("repo1");
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.totalElements()).isEqualTo(repos.size());
    }


    @Test
    @DisplayName("깃 허브 레포 페이지네이션 조회: 페이지 1")
    void getGithubRepoPageOne() {
        // given
        int page = 1;
        int pageSize = 7;
        Long userId = 1L;
        Pageable pageableForRequest = PageRequest.of(page, pageSize);

        List<GithubRepo> allRepos = List.of(
                new GithubRepo("repo1", "https://github.com/user/repo1", LocalDate.now()),
                new GithubRepo("repo2", "https://github.com/user/repo2", LocalDate.now()),
                new GithubRepo("repo3", "https://github.com/user/repo3", LocalDate.now()),
                new GithubRepo("repo4", "https://github.com/user/repo4", LocalDate.now()),
                new GithubRepo("repo5", "https://github.com/user/repo5", LocalDate.now()),
                new GithubRepo("repo6", "https://github.com/user/repo6", LocalDate.now()),
                new GithubRepo("repo7", "https://github.com/user/repo7", LocalDate.now()),
                new GithubRepo("repo8", "https://github.com/user/repo8", LocalDate.now())
        );
        long totalElements = allRepos.size();

        for (GithubRepo repo : allRepos) {
            repo.setUser(mockUser);
        }

        List<GithubRepo> contentForPageOne = List.of(allRepos.get(7));

        Page<GithubRepo> mockRepoPageFromRepository = new PageImpl<>(
                contentForPageOne,
                pageableForRequest,
                totalElements
        );

        List<GithubRepoPageResponse.GithubRepoSummary> summariesForPageOne = contentForPageOne.stream()
                .map(repo -> new GithubRepoPageResponse.GithubRepoSummary(
                        repo.getUser().getId(),
                        repo.getName(),
                        repo.getUrl(),
                        repo.getEndDate()))
                .toList();

        GithubRepoPageResponse expectedMapperResponse = new GithubRepoPageResponse(
                summariesForPageOne,
                userId,
                page,
                mockRepoPageFromRepository.getTotalPages(),
                totalElements
        );

        given(githubRepoRepository.findGithubRepoPages(eq(userId), eq(pageableForRequest)))
                .willReturn(mockRepoPageFromRepository);

        given(githubRepoMapper.toGithubRepoPageResponse(eq(mockRepoPageFromRepository), eq(userId)))
                .willReturn(expectedMapperResponse);

        // when
        GithubRepoPageResponse result = githubRepoService.getGithubRepoInfos(userId, page);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.totalElements()).isEqualTo(totalElements);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.repositoryId()).hasSize(1);
        assertThat(result.repositoryId().get(0).name()).isEqualTo("repo8");
    }

    @Test
    @DisplayName("요청한 깃허브 레포 페이지가 전체 페이지 수를 초과하면 예외를 던진다.")
    void throwsExceptionWhenGettingGithubRepoPageExceedsTotalPages() {
        // given
        Long userId = 1L;
        int pageSize = 7;
        int requestedInvalidPage = 2;
        Pageable pageableForInvalidRequest = PageRequest.of(requestedInvalidPage, pageSize);
        long totalElements = 8L;
        List<GithubRepo> emptyContentForInvalidPage = List.of();

        Page<GithubRepo> pageResultFromRepoForInvalidRequest = new PageImpl<>(
                emptyContentForInvalidPage,
                pageableForInvalidRequest,
                totalElements
        );
        given(githubRepoRepository.findGithubRepoPages(eq(userId), eq(pageableForInvalidRequest)))
                .willReturn(pageResultFromRepoForInvalidRequest);

        // when & then
        assertThatThrownBy(() -> githubRepoService.getGithubRepoInfos(userId, requestedInvalidPage))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(GithubRepoException.INVALID_GITHUBREPO_PAGE.getMessage());
    }

    @Test
    @DisplayName("요청한 깃허브 레포 페이지가 음수라면 예외를 던진다.")
    void throwsExceptionWhenGettingMinusGithubRepoPage() {
        // given
        Long userId = 1L;
        int requestedInvalidPage = -1;

        // when & then
        assertThatThrownBy(() -> githubRepoService.getGithubRepoInfos(userId, requestedInvalidPage))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(GithubRepoException.INVALID_GITHUBREPO_PAGE.getMessage());
    }
}