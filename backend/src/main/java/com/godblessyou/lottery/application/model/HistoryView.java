package com.godblessyou.lottery.application.model;

import com.godblessyou.lottery.domain.enums.PrizeType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HistoryView {

    private Long prizeItemId;
    private String prizeName;
    private PrizeType prizeType;
    private String content;
    private LocalDateTime createdAt;
}
