package com.specialwarriors.conal.feat.repo.dto.request;

import java.time.LocalDate;
import java.util.Set;

public record RepoCreateRequest(
    String name,
    String url,
    LocalDate endDate,
    Set<String> emails
) {

}
