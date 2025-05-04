package com.specialwarriors.conal.feat.repo.dto.request;

import java.time.LocalDate;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RepoCreateRequest {

    private String url;
    private LocalDate endDate;
    private Set<String> email;
}
