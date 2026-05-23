package com.godblessyou.lottery.application.model;

import com.godblessyou.lottery.domain.enums.PrizeType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DrawResultView {

    private Long prizeId;
    private String prizeName;
    private PrizeType prizeType;
    private String content;
    private Integer stockLeft;
}
