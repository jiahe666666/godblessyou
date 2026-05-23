package com.godblessyou.lottery.domain.repository;

import com.godblessyou.lottery.domain.entity.EmailVerification;
import java.util.Optional;

public interface EmailVerificationRepository {

    Optional<EmailVerification> findByToken(String token);

    Optional<EmailVerification> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
