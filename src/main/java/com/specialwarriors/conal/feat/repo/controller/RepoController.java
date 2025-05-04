package com.specialwarriors.conal.feat.repo.controller;

import com.specialwarriors.conal.feat.repo.service.RepoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RepoController {

    private final RepoService repoService;

}
