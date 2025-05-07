package com.specialwarriors.conal.contributor.controller;

import com.specialwarriors.conal.contributor.service.ContributorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ContributorRestController {

    private final ContributorService contributorService;

}
