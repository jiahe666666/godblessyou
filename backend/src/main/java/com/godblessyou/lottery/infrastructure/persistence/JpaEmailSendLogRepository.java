package com.godblessyou.lottery.infrastructure.persistence;

import com.godblessyou.lottery.domain.entity.EmailSendLog;
import com.godblessyou.lottery.domain.repository.EmailSendLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaEmailSendLogRepository extends JpaRepository<EmailSendLog, Long>, EmailSendLogRepository {

    @Override
    @Query("""
        select l from EmailSendLog l
        where l.status = com.godblessyou.lottery.domain.entity.EmailSendLog$Status.PENDING
          and l.retryCount < 3
          and l.nextRetryAt is not null
          and l.nextRetryAt <= :now
        order by l.createdAt asc
        """)
    List<EmailSendLog> findRetryableLogs(LocalDateTime now);
}
