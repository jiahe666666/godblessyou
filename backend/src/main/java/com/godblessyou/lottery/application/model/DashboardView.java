package com.godblessyou.lottery.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardView {

    private long prizeCount;
    private long enabledPrizeCount;
    private long stockLeftTotal;
    private long winnerCount;
}
