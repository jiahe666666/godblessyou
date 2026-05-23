package com.godblessyou.lottery.application.model;

import com.godblessyou.lottery.domain.enums.PrizeType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PrizeView {

    private Long id;
    private String name;
    private String description;
    private PrizeType prizeType;
    private Integer stockTotal;
    private Integer stockLeft;
    private BigDecimal probability;
    private boolean enabled;
}
