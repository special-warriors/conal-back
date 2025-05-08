package com.specialwarriors.conal.common.auth.jwt.dto;

public record TokenReissueRequest(String accessToken, String refreshToken) {

}
