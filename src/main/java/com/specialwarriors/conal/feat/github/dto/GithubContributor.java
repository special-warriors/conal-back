package com.specialwarriors.conal.feat.github.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubContributor {

    private String login;
    private String email;
}
