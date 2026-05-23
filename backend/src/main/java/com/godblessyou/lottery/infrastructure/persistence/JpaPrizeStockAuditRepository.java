package com.godblessyou.lottery.infrastructure.persistence;

import com.godblessyou.lottery.domain.entity.PrizeStockAudit;
import com.godblessyou.lottery.domain.repository.PrizeStockAuditRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPrizeStockAuditRepository extends JpaRepository<PrizeStockAudit, Long>, PrizeStockAuditRepository {
}
