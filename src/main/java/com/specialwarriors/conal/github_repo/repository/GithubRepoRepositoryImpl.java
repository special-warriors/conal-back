package com.specialwarriors.conal.github_repo.repository;

import static com.specialwarriors.conal.github_repo.domain.QGithubRepo.githubRepo;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GithubRepoRepositoryImpl implements GithubRepoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GithubRepo> findGithubRepoPages(Long userId, Pageable pageable) {

        List<GithubRepo> content = queryFactory
                .selectFrom(githubRepo)
                .where(githubRepo.user.id.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(githubRepo.count())
                .from(githubRepo)
                .where(githubRepo.user.id.eq(userId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

}
