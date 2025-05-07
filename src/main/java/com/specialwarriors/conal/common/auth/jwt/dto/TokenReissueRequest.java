package com.specialwarriors.conal.common.auth.jwt.dto;

import lombok.Getter;

@Getter
public class TokenReissueRequest {

    private String accessToken;
    private String refreshToken;

}
