package com.godblessyou.lottery.infrastructure.persistence;

import com.godblessyou.lottery.domain.entity.EmailVerification;
import com.godblessyou.lottery.domain.repository.EmailVerificationRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEmailVerificationRepository extends JpaRepository<EmailVerification, Long>, EmailVerificationRepository {
}
