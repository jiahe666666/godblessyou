package com.godblessyou.lottery.application.service;

import com.godblessyou.lottery.application.model.AuthTokenResponse;
import com.godblessyou.lottery.application.model.SimpleMessageResponse;
import com.godblessyou.lottery.interfaces.dto.auth.LoginRequest;
import com.godblessyou.lottery.interfaces.dto.auth.LogoutRequest;
import com.godblessyou.lottery.interfaces.dto.auth.RefreshRequest;
import com.godblessyou.lottery.interfaces.dto.auth.RegisterRequest;
import com.godblessyou.lottery.interfaces.dto.auth.ResendRequest;

public interface AuthService {

    SimpleMessageResponse register(RegisterRequest request);

    AuthTokenResponse login(LoginRequest request);

    AuthTokenResponse refresh(RefreshRequest request);

    SimpleMessageResponse logout(LogoutRequest request);

    SimpleMessageResponse verify(String token);

    SimpleMessageResponse resend(ResendRequest request);
}
