package com.specialwarriors.conal.contributor.service;

import com.specialwarriors.conal.contributor.domain.Contributor;
import com.specialwarriors.conal.contributor.repository.ContributorRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContributorQuery {

    private final ContributorRepository contributorRepository;

    public List<Contributor> findAll() {
        Iterable<Contributor> iterable = contributorRepository.findAll();
        return StreamSupport
            .stream(iterable.spliterator(), true)
            .collect(Collectors.toList());
    }
}
