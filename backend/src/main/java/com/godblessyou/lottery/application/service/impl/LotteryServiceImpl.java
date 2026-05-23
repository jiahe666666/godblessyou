package com.godblessyou.lottery.application.service.impl;

import com.godblessyou.lottery.application.model.DrawResultView;
import com.godblessyou.lottery.application.model.HistoryView;
import com.godblessyou.lottery.application.model.PrizeView;
import com.godblessyou.lottery.application.service.LotteryService;
import com.godblessyou.lottery.domain.entity.LotteryRecord;
import com.godblessyou.lottery.domain.entity.Prize;
import com.godblessyou.lottery.domain.entity.PrizeItem;
import com.godblessyou.lottery.domain.entity.User;
import com.godblessyou.lottery.infrastructure.audit.PrizeStockAuditService;
import com.godblessyou.lottery.infrastructure.persistence.JpaLotteryRecordRepository;
import com.godblessyou.lottery.infrastructure.persistence.JpaPrizeItemRepository;
import com.godblessyou.lottery.infrastructure.persistence.JpaPrizeRepository;
import com.godblessyou.lottery.infrastructure.persistence.JpaUserRepository;
import com.godblessyou.lottery.infrastructure.redis.RedisLockService;
import com.godblessyou.lottery.interfaces.advice.BusinessException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LotteryServiceImpl implements LotteryService {

    private final JpaPrizeRepository prizeRepository;
    private final JpaPrizeItemRepository prizeItemRepository;
    private final JpaLotteryRecordRepository lotteryRecordRepository;
    private final JpaUserRepository userRepository;
    private final RedisLockService lockService;
    private final PrizeStockAuditService prizeStockAuditService;

    @Value("${lottery.rules.daily-limit}")
    private int dailyLimit;

    @Value("${lottery.rules.cooldown-seconds}")
    private long cooldownSeconds;

    @Override
    public List<PrizeView> getPrizePool() {
        return prizeRepository.findEnabledAvailablePrizes().stream().map(this::toPrizeView).toList();
    }

    @Override
    @Transactional
    public DrawResultView draw(Long userId) {
        String lockKey = "lottery:lock:user:" + userId;
        if (!lockService.tryLock(lockKey, Duration.ofSeconds(10))) {
            throw new BusinessException("请求过于频繁，请稍后重试");
        }

        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
            validateUser(user);

            List<Prize> prizes = prizeRepository.findEnabledAvailablePrizes();
            if (prizes.isEmpty()) {
                throw new BusinessException("当前没有可抽取的奖品");
            }

            Prize selectedPrize = selectPrize(prizes);
            int stockBefore = selectedPrize.getStockLeft();
            if (prizeRepository.decrementStockIfAvailable(selectedPrize.getId()) != 1) {
                throw new BusinessException("手慢了，奖品已抽完");
            }

            PrizeItem prizeItem = prizeItemRepository.findTopByPrizeIdAndUsedFalseOrderByIdAsc(selectedPrize.getId())
                .orElseThrow(() -> new BusinessException("奖品库存项不存在"));
            prizeItem.setUsed(true);
            prizeItem.setUsedBy(userId);
            prizeItem.setUsedAt(LocalDateTime.now());
            prizeItemRepository.save(prizeItem);

            LotteryRecord record = new LotteryRecord();
            record.setUserId(userId);
            record.setPrizeId(selectedPrize.getId());
            record.setPrizeItemId(prizeItem.getId());
            lotteryRecordRepository.save(record);

            LocalDate today = LocalDate.now();
            if (user.getLastLotteryDate() == null || !today.equals(user.getLastLotteryDate())) {
                user.setDailyLotteryCount(0);
            }
            user.setLastLotteryDate(today);
            user.setLastLotteryAt(LocalDateTime.now());
            user.setDailyLotteryCount(user.getDailyLotteryCount() + 1);
            userRepository.save(user);

            Prize latestPrize = prizeRepository.findById(selectedPrize.getId()).orElse(selectedPrize);
            prizeStockAuditService.log(
                latestPrize.getId(),
                userId,
                "LOTTERY_DRAW",
                -1,
                stockBefore,
                latestPrize.getStockLeft(),
                "用户抽奖扣减库存，奖品项ID=" + prizeItem.getId()
            );
            return new DrawResultView(
                latestPrize.getId(),
                latestPrize.getName(),
                latestPrize.getPrizeType(),
                prizeItem.getContent(),
                latestPrize.getStockLeft()
            );
        } finally {
            lockService.unlock(lockKey);
        }
    }

    @Override
    public List<HistoryView> getHistory(Long userId) {
        List<HistoryView> history = new ArrayList<>();
        for (PrizeItem item : prizeItemRepository.findByUsedByOrderByCreatedAtDesc(userId)) {
            Prize prize = prizeRepository.findById(item.getPrizeId()).orElse(null);
            if (prize == null) {
                continue;
            }
            history.add(new HistoryView(item.getId(), prize.getName(), prize.getPrizeType(), item.getContent(), item.getUsedAt()));
        }
        return history;
    }

    private void validateUser(User user) {
        if (!user.isVerified()) {
            throw new BusinessException("邮箱未验证，无法参与抽奖");
        }
        LocalDate today = LocalDate.now();
        if (user.getLastLotteryDate() != null && !today.equals(user.getLastLotteryDate())) {
            user.setDailyLotteryCount(0);
        }
        if (user.getDailyLotteryCount() >= dailyLimit) {
            throw new BusinessException("今日抽奖次数已用完");
        }
        if (user.getLastLotteryAt() != null && user.getLastLotteryAt().plusSeconds(cooldownSeconds).isAfter(LocalDateTime.now())) {
            throw new BusinessException("抽奖冷却中，请稍后再试");
        }
    }

    private Prize selectPrize(List<Prize> prizes) {
        double total = prizes.stream().mapToDouble(prize -> prize.getProbability().doubleValue()).sum();
        double random = ThreadLocalRandom.current().nextDouble(total);
        double cursor = 0;
        for (Prize prize : prizes) {
            cursor += prize.getProbability().doubleValue();
            if (random <= cursor) {
                return prize;
            }
        }
        return prizes.get(prizes.size() - 1);
    }

    private PrizeView toPrizeView(Prize prize) {
        return new PrizeView(
            prize.getId(),
            prize.getName(),
            prize.getDescription(),
            prize.getPrizeType(),
            prize.getStockTotal(),
            prize.getStockLeft(),
            prize.getProbability(),
            prize.isEnabled()
        );
    }
}
