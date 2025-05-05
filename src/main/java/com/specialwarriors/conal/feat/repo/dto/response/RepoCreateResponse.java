package com.specialwarriors.conal.feat.repo.dto.response;

import java.time.LocalDate;

public record RepoCreateResponse(
    String name,
    String url,
    LocalDate endDate
) {

}
