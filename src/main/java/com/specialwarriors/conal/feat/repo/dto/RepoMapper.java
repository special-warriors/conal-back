package com.specialwarriors.conal.feat.repo.dto;


import com.specialwarriors.conal.feat.repo.domain.Repo;
import com.specialwarriors.conal.feat.repo.dto.request.RepoCreateRequest;
import com.specialwarriors.conal.feat.repo.dto.response.RepoCreateResponse;
import com.specialwarriors.conal.feat.repo.dto.response.RepoPageResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Slice;

@Mapper(componentModel = "spring")
public interface RepoMapper {

    Repo toRepo(RepoCreateRequest request);

    RepoCreateResponse toRepoCreateResponse(Repo repo);


    RepoPageResponse.RepoSummary toRepoSummary(Repo repo);

    List<RepoPageResponse.RepoSummary> toRepoSummaryList(List<Repo> repos);

    default RepoPageResponse toRepoPageResponse(Slice<Repo> slice) {
        return new RepoPageResponse(
            toRepoSummaryList(slice.getContent()),
            slice.hasNext()
        );
    }
}
