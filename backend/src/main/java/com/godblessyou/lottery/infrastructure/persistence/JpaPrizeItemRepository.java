package com.godblessyou.lottery.infrastructure.persistence;

import com.godblessyou.lottery.domain.entity.PrizeItem;
import com.godblessyou.lottery.domain.repository.PrizeItemRepository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface JpaPrizeItemRepository extends JpaRepository<PrizeItem, Long>, PrizeItemRepository {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PrizeItem> findTopByPrizeIdAndUsedFalseOrderByIdAsc(Long prizeId);

    @Override
    List<PrizeItem> findByUsedByOrderByCreatedAtDesc(Long userId);

    @Override
    void deleteByPrizeId(Long prizeId);
}
