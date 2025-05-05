package com.specialwarriors.conal.feat.repo.dto;


import com.specialwarriors.conal.feat.repo.domain.Repo;
import com.specialwarriors.conal.feat.repo.dto.request.RepoCreateRequest;
import com.specialwarriors.conal.feat.repo.dto.response.RepoCreateResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RepoMapper {

    Repo toRepo(RepoCreateRequest request);

    RepoCreateResponse toRepoCreateResponse(Repo repo);

}
