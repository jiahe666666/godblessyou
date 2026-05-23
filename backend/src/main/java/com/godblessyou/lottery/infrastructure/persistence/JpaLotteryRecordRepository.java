package com.godblessyou.lottery.infrastructure.persistence;

import com.godblessyou.lottery.domain.entity.LotteryRecord;
import com.godblessyou.lottery.domain.repository.LotteryRecordRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLotteryRecordRepository extends JpaRepository<LotteryRecord, Long>, LotteryRecordRepository {
}
