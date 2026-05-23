package com.godblessyou.lottery.infrastructure.audit;

import com.godblessyou.lottery.domain.entity.PrizeStockAudit;
import com.godblessyou.lottery.infrastructure.persistence.JpaPrizeStockAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrizeStockAuditService {

    private final JpaPrizeStockAuditRepository prizeStockAuditRepository;

    public void log(
        Long prizeId,
        Long operatorUserId,
        String changeType,
        int delta,
        int stockBefore,
        int stockAfter,
        String remark
    ) {
        PrizeStockAudit audit = new PrizeStockAudit();
        audit.setPrizeId(prizeId);
        audit.setOperatorUserId(operatorUserId);
        audit.setChangeType(changeType);
        audit.setDelta(delta);
        audit.setStockBefore(stockBefore);
        audit.setStockAfter(stockAfter);
        audit.setRemark(remark);
        prizeStockAuditRepository.save(audit);
    }
}
