package com.godblessyou.lottery.application.service;

import com.godblessyou.lottery.application.model.DrawResultView;
import com.godblessyou.lottery.application.model.HistoryView;
import com.godblessyou.lottery.application.model.PrizeView;
import java.util.List;

public interface LotteryService {

    List<PrizeView> getPrizePool();

    DrawResultView draw(Long userId);

    List<HistoryView> getHistory(Long userId);
}
