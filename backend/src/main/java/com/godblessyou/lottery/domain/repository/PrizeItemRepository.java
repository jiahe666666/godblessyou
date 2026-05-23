package com.godblessyou.lottery.domain.repository;

import com.godblessyou.lottery.domain.entity.PrizeItem;
import java.util.List;
import java.util.Optional;

public interface PrizeItemRepository {

    Optional<PrizeItem> findTopByPrizeIdAndUsedFalseOrderByIdAsc(Long prizeId);

    List<PrizeItem> findByUsedByOrderByCreatedAtDesc(Long userId);

    void deleteByPrizeId(Long prizeId);
}