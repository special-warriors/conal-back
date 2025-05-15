package com.specialwarriors.conal.github_repo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.specialwarriors.conal.common.config.QuerydslConfig;
import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.exception.GithubRepoException;
import com.specialwarriors.conal.github_repo.repository.GithubRepoRepository;
import com.specialwarriors.conal.user.repository.UserRepository;
import com.specialwarriors.conal.user.service.UserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@ActiveProfiles("test")
@DataJpaTest
@Import(QuerydslConfig.class)
class GithubRepoQueryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GithubRepoRepository githubRepoRepository;

    private GithubRepoQuery githubRepoQuery;

    @BeforeEach
    void init() {
        UserQuery userQuery = new UserQuery(userRepository);
        githubRepoQuery = new GithubRepoQuery(githubRepoRepository, userQuery);
    }

    @Nested
    @DisplayName("ID로 Github Repository를 조회할 때 ")
    @Sql(scripts = "/sql/github_repo/service/find_by_repository_id_test_setup.sql",
            executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
    class FindByIdTest {

        @DisplayName("성공한다.")
        @Test
        public void success() {
            // given
            long repositoryId = 1L;

            // when
            GithubRepo githubRepo = githubRepoQuery.findById(repositoryId);

            // then
            assertThat(githubRepo.getId()).isEqualTo(repositoryId);
        }

        @DisplayName("존재하지 않는 Repository일 경우 예외가 발생한다.")
        @Test
        public void githubRepoNotFound() {
            // given
            long repositoryId = 2L;

            // when

            // then
            assertThatThrownBy(() -> githubRepoQuery.findById(repositoryId))
                    .isInstanceOf(GeneralException.class)
                    .extracting("exception")
                    .isEqualTo(GithubRepoException.GITHUB_REPO_NOT_FOUND);
        }
    }
}
