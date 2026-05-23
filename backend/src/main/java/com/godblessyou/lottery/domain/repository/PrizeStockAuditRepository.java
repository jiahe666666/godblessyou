package com.godblessyou.lottery.domain.repository;

import com.godblessyou.lottery.domain.entity.PrizeStockAudit;
import java.util.List;

public interface PrizeStockAuditRepository {

    List<PrizeStockAudit> findTop100ByOrderByCreatedAtDesc();
}
