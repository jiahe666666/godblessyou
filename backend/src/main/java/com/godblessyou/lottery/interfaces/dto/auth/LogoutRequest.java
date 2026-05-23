package com.godblessyou.lottery.interfaces.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequest {

    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
