package com.specialwarriors.conal.feat.repo.repository;

import static com.specialwarriors.conal.feat.repo.domain.QRepo.repo;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.specialwarriors.conal.feat.repo.domain.Repo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class RepoRepositoryImpl implements RepoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Repo> searchRepoPages(Long lastRepoId, Pageable pageable) {
        BooleanExpression where = lastRepoId != null ? repo.id.lt(lastRepoId) : null;

        List<Repo> result = queryFactory
            .selectFrom(repo)
            .where(where)
            .orderBy(repo.id.desc())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        boolean hasNext = result.size() > pageable.getPageSize();
        if (hasNext) {
            result.remove(pageable.getPageSize());
        }

        return new SliceImpl<>(result, pageable, hasNext);
    }
}
