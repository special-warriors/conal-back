package com.specialwarriors.conal.feat.repo.dto.response;

import java.time.LocalDate;
import java.util.List;

public record RepoPageResponse(
    List<RepoSummary> repos,
    boolean hasNext
) {

    public record RepoSummary(
        Long id,
        String name,
        String url,
        LocalDate endDate
    ) {

    }
}
