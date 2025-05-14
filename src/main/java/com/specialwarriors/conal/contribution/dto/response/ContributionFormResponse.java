package com.specialwarriors.conal.contribution.dto.response;

public record ContributionFormResponse(
    Long userId,
    Long repoId,
    String email
) {

}