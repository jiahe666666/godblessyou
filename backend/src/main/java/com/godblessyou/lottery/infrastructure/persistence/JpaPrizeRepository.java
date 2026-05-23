package com.godblessyou.lottery.infrastructure.persistence;

import com.godblessyou.lottery.domain.entity.Prize;
import com.godblessyou.lottery.domain.repository.PrizeRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaPrizeRepository extends JpaRepository<Prize, Long>, PrizeRepository {

    @Override
    List<Prize> findAllByOrderByCreatedAtDesc();

    @Override
    @Query("select p from Prize p where p.enabled = true and p.stockLeft > 0 order by p.createdAt asc")
    List<Prize> findEnabledAvailablePrizes();

    @Override
    @Modifying
    @Query("update Prize p set p.stockLeft = p.stockLeft - 1 where p.id = :prizeId and p.stockLeft > 0")
    int decrementStockIfAvailable(@Param("prizeId") Long prizeId);
}
