package com.godblessyou.lottery.application.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PrizeStockAuditView {

    private Long id;
    private Long prizeId;
    private String prizeName;
    private Long operatorUserId;
    private String changeType;
    private Integer delta;
    private Integer stockBefore;
    private Integer stockAfter;
    private String remark;
    private LocalDateTime createdAt;
}
