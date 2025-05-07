package com.specialwarriors.conal.github_repo.dto;


import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.dto.request.GithubRepoCreateRequest;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoCreateResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoDeleteResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoGetResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoPageResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoPageResponse.GithubRepoSummary;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface GithubRepoMapper {

    GithubRepo toGithubRepo(GithubRepoCreateRequest request);

    GithubRepoCreateResponse toGithubRepoCreateResponse(GithubRepo repo);

    GithubRepoGetResponse toGithubRepoGetResponse(GithubRepo repo);

    GithubRepoSummary toGithubRepoSummary(GithubRepo repo);

    default GithubRepoPageResponse toGithubRepoPageResponse(Page<GithubRepo> page) {
        return new GithubRepoPageResponse(
            page.getContent().stream()
                .map(this::toGithubRepoSummary)
                .toList(),
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements()
        );
    }

    default GithubRepoDeleteResponse toGithubDeleteRepoResponse() {
        return new GithubRepoDeleteResponse(
            true
        );
    }

}
