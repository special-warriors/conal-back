package com.specialwarriors.conal.contributor.repository;

import com.specialwarriors.conal.contributor.domain.Contributor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContributorRepository extends CrudRepository<Contributor, Long> {

}
