package com.specialwarriors.conal.feat.repo.service;

import com.specialwarriors.conal.feat.contributor.domain.Contributor;
import com.specialwarriors.conal.feat.contributor.repository.ContributorRepository;
import com.specialwarriors.conal.feat.repo.domain.Repo;
import com.specialwarriors.conal.feat.repo.dto.request.RepoCreateRequest;
import com.specialwarriors.conal.feat.repo.dto.response.RepoCreateResponse;
import com.specialwarriors.conal.feat.repo.repository.RepoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RepoService {

    private final RepoRepository repoRepository;
    private final ContributorRepository contributorRepository;

    @Transactional
    public RepoCreateResponse createRepo(RepoCreateRequest request) {

        List<Contributor> contributors = request.getEmail().stream()
            .map(e -> Contributor.builder().email(e).build()).toList();
        contributors = (List<Contributor>) contributorRepository.saveAll(contributors);

        Repo repo = Repo.builder()
            .url(request.getUrl())
            .endDate(request.getEndDate())
            .contributors(contributors)
            .build();

        repo = repoRepository.save(repo);

        return null;

    }


}
