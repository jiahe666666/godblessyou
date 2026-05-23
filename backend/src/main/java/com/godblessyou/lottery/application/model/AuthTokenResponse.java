package com.godblessyou.lottery.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthTokenResponse {

    private String accessToken;
    private String refreshToken;
    private String username;
    private boolean verified;
    private boolean admin;
}
