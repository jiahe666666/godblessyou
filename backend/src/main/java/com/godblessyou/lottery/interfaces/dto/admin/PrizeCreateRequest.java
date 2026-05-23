package com.godblessyou.lottery.interfaces.dto.admin;

import com.godblessyou.lottery.domain.enums.PrizeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrizeCreateRequest {

    @NotBlank(message = "奖品名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "奖品类型不能为空")
    private PrizeType prizeType;

    @NotNull(message = "概率不能为空")
    @DecimalMin(value = "0.00000001", message = "概率必须大于 0")
    private BigDecimal probability;

    @NotBlank(message = "批量内容不能为空")
    private String contents;

    private Boolean enabled = Boolean.TRUE;
}
