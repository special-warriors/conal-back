package com.specialwarriors.conal.feat.repo.repository;

import com.specialwarriors.conal.feat.repo.domain.Repo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface RepoRepositoryCustom {

    Slice<Repo> searchRepoPages(Long lastRepoId, Pageable pageable);
}
