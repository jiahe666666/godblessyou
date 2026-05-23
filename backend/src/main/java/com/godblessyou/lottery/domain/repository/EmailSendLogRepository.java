package com.godblessyou.lottery.domain.repository;

import com.godblessyou.lottery.domain.entity.EmailSendLog;
import java.time.LocalDateTime;
import java.util.List;

public interface EmailSendLogRepository {

    List<EmailSendLog> findRetryableLogs(LocalDateTime now);
}
