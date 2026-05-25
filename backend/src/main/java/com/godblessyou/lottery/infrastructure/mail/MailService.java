package com.godblessyou.lottery.infrastructure.mail;

import com.godblessyou.lottery.domain.entity.EmailSendLog;
import com.godblessyou.lottery.domain.entity.User;
import com.godblessyou.lottery.infrastructure.persistence.JpaEmailSendLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final JpaEmailSendLogRepository emailSendLogRepository;

    @Value("${spring.mail.username}")
    private String smtpUsername;

    @Value("${lottery.mail.from:#{null}}")
    private String mailFrom;

    @Value("${lottery.frontend-base-url}")
    private String frontendBaseUrl;

    private String getFrom() {
        if (StringUtils.hasText(mailFrom)) {
            return mailFrom;
        }
        return smtpUsername;
    }

    @Async("mailTaskExecutor")
    public void sendVerificationEmail(User user, String token) {
        String subject = "抽奖系统邮箱验证";
        String content = "请点击链接完成邮箱验证：" + frontendBaseUrl + "/verify?token=" + token;
        try {
            sendMail(user.getEmail(), subject, content);
            log.info("Verification email sent to {} (userId={})", user.getEmail(), user.getId());
        } catch (Exception ex) {
            log.warn("Failed to send verification email to {}: {}", user.getEmail(), ex.getMessage());
            EmailSendLog emailSendLog = new EmailSendLog();
            emailSendLog.setUserId(user.getId());
            emailSendLog.setEmail(user.getEmail());
            emailSendLog.setSubject(subject);
            emailSendLog.setContentText(content);
            emailSendLog.setStatus(EmailSendLog.Status.PENDING);
            emailSendLog.setRetryCount(0);
            emailSendLog.setNextRetryAt(LocalDateTime.now().plusMinutes(5));
            emailSendLog.setLastError(trimError(ex));
            emailSendLogRepository.save(emailSendLog);
        }
    }

    @Scheduled(fixedDelayString = "${lottery.mail.retry-delay-ms:300000}")
    @Transactional
    public void retryPendingEmails() {
        List<EmailSendLog> retryableLogs = emailSendLogRepository.findRetryableLogs(LocalDateTime.now());
        if (retryableLogs.isEmpty()) {
            return;
        }
        log.info("Retrying {} pending emails", retryableLogs.size());
        for (EmailSendLog logEntry : retryableLogs) {
            try {
                sendMail(logEntry.getEmail(), logEntry.getSubject(), logEntry.getContentText());
                logEntry.setStatus(EmailSendLog.Status.SENT);
                logEntry.setNextRetryAt(null);
                logEntry.setLastError(null);
                emailSendLogRepository.save(logEntry);
                log.info("Retry mail sent to {} (logId={})", logEntry.getEmail(), logEntry.getId());
            } catch (Exception ex) {
                int nextRetryCount = logEntry.getRetryCount() + 1;
                logEntry.setRetryCount(nextRetryCount);
                logEntry.setLastError(trimError(ex));
                if (nextRetryCount >= 3) {
                    logEntry.setStatus(EmailSendLog.Status.FAILED);
                    logEntry.setNextRetryAt(null);
                } else {
                    logEntry.setStatus(EmailSendLog.Status.PENDING);
                    logEntry.setNextRetryAt(LocalDateTime.now().plusMinutes(5));
                }
                emailSendLogRepository.save(logEntry);
                log.warn("Retry mail failed for {} (attempt {}): {}", logEntry.getEmail(), nextRetryCount, ex.getMessage());
            }
        }
    }

    private void sendMail(String email, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getFrom());
        message.setTo(email);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    private String trimError(Exception ex) {
        String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}