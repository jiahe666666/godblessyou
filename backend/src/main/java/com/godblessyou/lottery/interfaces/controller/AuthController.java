package com.godblessyou.lottery.interfaces.controller;

import com.godblessyou.lottery.application.model.AuthTokenResponse;
import com.godblessyou.lottery.application.model.SimpleMessageResponse;
import com.godblessyou.lottery.application.service.AuthService;
import com.godblessyou.lottery.interfaces.advice.ApiResponse;
import com.godblessyou.lottery.interfaces.dto.auth.LoginRequest;
import com.godblessyou.lottery.interfaces.dto.auth.LogoutRequest;
import com.godblessyou.lottery.interfaces.dto.auth.RefreshRequest;
import com.godblessyou.lottery.interfaces.dto.auth.RegisterRequest;
import com.godblessyou.lottery.interfaces.dto.auth.ResendRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "注册")
    @PostMapping("/register")
    public ApiResponse<SimpleMessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResponse<SimpleMessageResponse> logout(@Valid @RequestBody LogoutRequest request) {
        return ApiResponse.success(authService.logout(request));
    }

    @Operation(summary = "邮箱验证")
    @GetMapping("/verify")
    public ApiResponse<SimpleMessageResponse> verify(@RequestParam String token) {
        return ApiResponse.success(authService.verify(token));
    }

    @Operation(summary = "重新发送验证邮件")
    @PostMapping("/resend")
    public ApiResponse<SimpleMessageResponse> resend(@Valid @RequestBody ResendRequest request) {
        return ApiResponse.success(authService.resend(request));
    }
}
