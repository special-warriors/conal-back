package com.specialwarriors.conal.github_repo.dto;


import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.dto.request.GithubRepoCreateRequest;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoCreateResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoGetResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoPageResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoPageResponse.GithubRepoSummary;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface GithubRepoMapper {

    GithubRepo toGithubRepo(GithubRepoCreateRequest request);

    default GithubRepoCreateResponse toGithubRepoCreateResponse(String owner, String reponame) {

        return new GithubRepoCreateResponse(
                owner,
                reponame
        );
    }

    default GithubRepoGetResponse toGithubRepoGetResponse(GithubRepo repo, String owner,
            String reponame, Long userId) {

        return new GithubRepoGetResponse(
                userId,
                repo.getId(),
                repo.getName(),
                repo.getUrl(),
                repo.getEndDate(),
                owner,
                reponame
        );
    }


    GithubRepoSummary toGithubRepoSummary(GithubRepo repo);

    default GithubRepoPageResponse toGithubRepoPageResponse(Page<GithubRepo> page, Long userId) {

        return new GithubRepoPageResponse(
                page.getContent().stream()
                        .map(this::toGithubRepoSummary)
                        .toList(),
                userId,
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }

}
