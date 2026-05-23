package com.godblessyou.lottery.infrastructure.config;

import com.godblessyou.lottery.domain.entity.User;
import com.godblessyou.lottery.infrastructure.persistence.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminBootstrapConfig {

    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${lottery.bootstrap.admin.username:}")
    private String adminUsername;

    @Value("${lottery.bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${lottery.bootstrap.admin.password:}")
    private String adminPassword;

    @Bean
    public ApplicationRunner adminBootstrapRunner() {
        return args -> {
            if (!StringUtils.hasText(adminUsername) || !StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
                log.info("Admin bootstrap skipped because required properties are missing");
                return;
            }
            if (userRepository.existsByUsername(adminUsername) || userRepository.existsByEmail(adminEmail.toLowerCase())) {
                log.info("Admin bootstrap skipped because target admin already exists");
                return;
            }

            User admin = new User();
            admin.setUsername(adminUsername.trim());
            admin.setEmail(adminEmail.trim().toLowerCase());
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setVerified(true);
            admin.setAdmin(true);
            admin.setDailyLotteryCount(0);
            userRepository.save(admin);
            log.info("Bootstrap admin account created: {}", admin.getUsername());
        };
    }
}
