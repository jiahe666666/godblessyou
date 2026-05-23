package com.godblessyou.lottery.application.service.impl;

import com.godblessyou.lottery.application.model.AuthTokenResponse;
import com.godblessyou.lottery.application.model.SimpleMessageResponse;
import com.godblessyou.lottery.application.service.AuthService;
import com.godblessyou.lottery.domain.entity.EmailVerification;
import com.godblessyou.lottery.domain.entity.User;
import com.godblessyou.lottery.infrastructure.mail.MailService;
import com.godblessyou.lottery.infrastructure.persistence.JpaEmailVerificationRepository;
import com.godblessyou.lottery.infrastructure.persistence.JpaUserRepository;
import com.godblessyou.lottery.infrastructure.redis.RedisRateLimitService;
import com.godblessyou.lottery.infrastructure.security.JwtService;
import com.godblessyou.lottery.interfaces.advice.BusinessException;
import com.godblessyou.lottery.interfaces.dto.auth.LoginRequest;
import com.godblessyou.lottery.interfaces.dto.auth.LogoutRequest;
import com.godblessyou.lottery.interfaces.dto.auth.RefreshRequest;
import com.godblessyou.lottery.interfaces.dto.auth.RegisterRequest;
import com.godblessyou.lottery.interfaces.dto.auth.ResendRequest;
import io.jsonwebtoken.JwtException;
import java.time.Instant;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JpaUserRepository userRepository;
    private final JpaEmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;
    private final RedisRateLimitService rateLimitService;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${lottery.jwt.refresh-expire-seconds}")
    private long refreshExpireSeconds;

    @Value("${lottery.mail.verification-expire-minutes:10}")
    private int verificationExpireMinutes;

    @Override
    @Transactional
    public SimpleMessageResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setVerified(false);
        user.setAdmin(false);
        user.setDailyLotteryCount(0);
        user = userRepository.save(user);

        String token = UUID.randomUUID().toString().replace("-", "");
        EmailVerification verification = new EmailVerification();
        verification.setUserId(user.getId());
        verification.setToken(token);
        verification.setUsed(false);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(verificationExpireMinutes));
        emailVerificationRepository.save(verification);
        mailService.sendVerificationEmail(user, token);
        return new SimpleMessageResponse("注册成功，请查看邮箱完成验证（" + verificationExpireMinutes + "分钟内有效）");
    }

    @Override
    public AuthTokenResponse login(LoginRequest request) {
        if (!rateLimitService.tryAcquire("login:" + request.getAccount(), 5, Duration.ofMinutes(1))) {
            throw new BusinessException("登录过于频繁，请稍后再试");
        }
        User user = loadUserByAccount(request.getAccount());
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("账号或密码错误");
        }
        return issueTokens(user);
    }

    @Override
    public AuthTokenResponse refresh(RefreshRequest request) {
        try {
            if (!jwtService.isRefreshToken(request.getRefreshToken())) {
                throw new BusinessException("refreshToken 非法");
            }
            if (isBlacklisted(request.getRefreshToken())) {
                throw new BusinessException("refreshToken 已失效");
            }
            Long userId = jwtService.extractUserId(request.getRefreshToken());
            String cached = stringRedisTemplate.opsForValue().get(refreshKey(userId));
            if (cached == null || !cached.equals(request.getRefreshToken())) {
                throw new BusinessException("refreshToken 已失效");
            }
            User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
            return issueTokens(user);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException("refreshToken 非法或已过期");
        }
    }

    @Override
    public SimpleMessageResponse logout(LogoutRequest request) {
        try {
            if (!jwtService.isRefreshToken(request.getRefreshToken())) {
                throw new BusinessException("refreshToken 非法");
            }
            Long userId = jwtService.extractUserId(request.getRefreshToken());
            Duration ttl = remainingTtl(request.getRefreshToken());
            if (!ttl.isNegative() && !ttl.isZero()) {
                stringRedisTemplate.opsForValue().set(refreshBlacklistKey(request.getRefreshToken()), "1", ttl);
            }
            String cached = stringRedisTemplate.opsForValue().get(refreshKey(userId));
            if (request.getRefreshToken().equals(cached)) {
                stringRedisTemplate.delete(refreshKey(userId));
            }
            return new SimpleMessageResponse("已退出登录");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException("refreshToken 非法或已过期");
        }
    }

    @Override
    @Transactional
    public SimpleMessageResponse verify(String token) {
        EmailVerification verification = emailVerificationRepository.findByToken(token)
            .orElseThrow(() -> new BusinessException("验证链接无效"));
        if (verification.isUsed()) {
            throw new BusinessException("验证链接已被使用");
        }
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("验证链接已过期");
        }
        User user = userRepository.findById(verification.getUserId())
            .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setVerified(true);
        verification.setUsed(true);
        userRepository.save(user);
        emailVerificationRepository.save(verification);
        return new SimpleMessageResponse("邮箱验证成功");
    }

    @Override
    @Transactional
    public SimpleMessageResponse resend(ResendRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (!rateLimitService.tryAcquire("resend:" + normalizedEmail, 1, Duration.ofMinutes(1))) {
            throw new BusinessException("验证邮件发送过于频繁，请稍后再试");
        }
        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new BusinessException("邮箱未注册"));
        if (user.isVerified()) {
            return new SimpleMessageResponse("该邮箱已完成验证");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        EmailVerification verification = new EmailVerification();
        verification.setUserId(user.getId());
        verification.setToken(token);
        verification.setUsed(false);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(verificationExpireMinutes));
        emailVerificationRepository.save(verification);
        mailService.sendVerificationEmail(user, token);
        return new SimpleMessageResponse("验证邮件已重新发送（" + verificationExpireMinutes + "分钟内有效）");
    }

    private User loadUserByAccount(String account) {
        Optional<User> user = account.contains("@")
            ? userRepository.findByEmail(account.trim().toLowerCase())
            : userRepository.findByUsername(account.trim());
        return user.orElseThrow(() -> new BusinessException("账号或密码错误"));
    }

    private AuthTokenResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        stringRedisTemplate.opsForValue().set(refreshKey(user.getId()), refreshToken, Duration.ofSeconds(refreshExpireSeconds));
        return new AuthTokenResponse(accessToken, refreshToken, user.getUsername(), user.isVerified(), user.isAdmin());
    }

    private String refreshKey(Long userId) {
        return "refresh:user:" + userId;
    }

    private String refreshBlacklistKey(String refreshToken) {
        return "refresh:blacklist:" + refreshToken;
    }

    private boolean isBlacklisted(String refreshToken) {
        Boolean exists = stringRedisTemplate.hasKey(refreshBlacklistKey(refreshToken));
        return Boolean.TRUE.equals(exists);
    }

    private Duration remainingTtl(String refreshToken) {
        Instant expiration = jwtService.parseClaims(refreshToken).getExpiration().toInstant();
        return Duration.between(Instant.now(), expiration);
    }
}
