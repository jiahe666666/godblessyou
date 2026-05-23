package com.godblessyou.lottery.domain.repository;

import com.godblessyou.lottery.domain.entity.Prize;
import java.util.List;

public interface PrizeRepository {

    List<Prize> findAllByOrderByCreatedAtDesc();

    List<Prize> findEnabledAvailablePrizes();

    int decrementStockIfAvailable(Long prizeId);
}
