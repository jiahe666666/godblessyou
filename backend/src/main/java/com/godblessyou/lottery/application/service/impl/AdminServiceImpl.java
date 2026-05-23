package com.godblessyou.lottery.application.service.impl;

import com.godblessyou.lottery.application.model.DashboardView;
import com.godblessyou.lottery.application.model.PrizeStockAuditView;
import com.godblessyou.lottery.application.model.PrizeView;
import com.godblessyou.lottery.application.model.SimpleMessageResponse;
import com.godblessyou.lottery.application.service.AdminService;
import com.godblessyou.lottery.domain.entity.Prize;
import com.godblessyou.lottery.domain.entity.PrizeItem;
import com.godblessyou.lottery.infrastructure.audit.PrizeStockAuditService;
import com.godblessyou.lottery.infrastructure.persistence.JpaLotteryRecordRepository;
import com.godblessyou.lottery.infrastructure.persistence.JpaPrizeItemRepository;
import com.godblessyou.lottery.infrastructure.persistence.JpaPrizeRepository;
import com.godblessyou.lottery.infrastructure.persistence.JpaPrizeStockAuditRepository;
import com.godblessyou.lottery.interfaces.advice.BusinessException;
import com.godblessyou.lottery.interfaces.dto.admin.PrizeCreateRequest;
import com.godblessyou.lottery.interfaces.dto.admin.PrizeUpdateRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final JpaPrizeRepository prizeRepository;
    private final JpaPrizeItemRepository prizeItemRepository;
    private final JpaLotteryRecordRepository lotteryRecordRepository;
    private final JpaPrizeStockAuditRepository prizeStockAuditRepository;
    private final PrizeStockAuditService prizeStockAuditService;

    @Override
    public DashboardView getDashboard() {
        List<Prize> prizes = prizeRepository.findAllByOrderByCreatedAtDesc();
        long enabledPrizeCount = prizes.stream().filter(Prize::isEnabled).count();
        long stockLeftTotal = prizes.stream().mapToLong(Prize::getStockLeft).sum();
        return new DashboardView(prizes.size(), enabledPrizeCount, stockLeftTotal, lotteryRecordRepository.count());
    }

    @Override
    public List<PrizeView> getPrizes() {
        return prizeRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toPrizeView).toList();
    }

    @Override
    @Transactional
    public PrizeView createPrize(PrizeCreateRequest request) {
        List<String> contents = request.getContents().lines().map(String::trim).filter(line -> !line.isEmpty()).toList();
        if (contents.isEmpty()) {
            throw new BusinessException("库存内容不能为空");
        }

        BigDecimal newProbability = request.getProbability();
        BigDecimal totalProbability = prizeRepository.findAll().stream()
            .filter(Prize::isEnabled)
            .map(Prize::getProbability)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .add(Boolean.TRUE.equals(request.getEnabled()) ? newProbability : BigDecimal.ZERO);

        if (totalProbability.compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessException(
                String.format("概率权重总和超过 1.0（当前已启用奖品权重合计 %.8f，新增奖品权重 %.8f）",
                    totalProbability.subtract(newProbability).doubleValue(), newProbability.doubleValue()));
        }
        if (totalProbability.compareTo(BigDecimal.ONE) < 0) {
            log.warn("概率权重总和为 {}，小于 1.0，未中奖概率为 {}", totalProbability,
                BigDecimal.ONE.subtract(totalProbability));
        }

        Prize prize = new Prize();
        prize.setName(request.getName());
        prize.setDescription(request.getDescription());
        prize.setPrizeType(request.getPrizeType());
        prize.setProbability(newProbability);
        prize.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
        prize.setStockTotal(contents.size());
        prize.setStockLeft(contents.size());
        prize = prizeRepository.save(prize);

        Long prizeId = prize.getId();
        List<PrizeItem> items = contents.stream().map(content -> {
            PrizeItem item = new PrizeItem();
            item.setPrizeId(prizeId);
            item.setContent(content);
            item.setUsed(false);
            return item;
        }).toList();
        prizeItemRepository.saveAll(items);
        prizeStockAuditService.log(
            prize.getId(),
            null,
            "ADMIN_CREATE",
            contents.size(),
            0,
            prize.getStockLeft(),
            "管理员创建奖品并初始化库存"
        );
        return toPrizeView(prize);
    }

    @Override
    @Transactional
    public PrizeView updatePrize(Long prizeId, PrizeUpdateRequest request) {
        Prize prize = prizeRepository.findById(prizeId)
            .orElseThrow(() -> new BusinessException("奖品不存在"));

        BigDecimal oldProbability = prize.getProbability();
        BigDecimal newProbability = request.getProbability();
        boolean wasEnabled = prize.isEnabled();
        boolean newEnabled = request.getEnabled() != null ? request.getEnabled() : wasEnabled;

        // Recalculate total probability considering the update
        BigDecimal totalOthers = prizeRepository.findAll().stream()
            .filter(Prize::isEnabled)
            .filter(p -> !p.getId().equals(prizeId))
            .map(Prize::getProbability)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProbability = totalOthers.add(newEnabled ? newProbability : BigDecimal.ZERO);

        if (totalProbability.compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessException(
                String.format("概率权重总和超过 1.0（其他启用奖品权重合计 %.8f，本次奖品权重 %.8f）",
                    totalOthers.doubleValue(), newProbability.doubleValue()));
        }

        prize.setName(request.getName());
        prize.setDescription(request.getDescription());
        prize.setPrizeType(request.getPrizeType());
        prize.setProbability(newProbability);
        prize.setEnabled(newEnabled);
        prize = prizeRepository.save(prize);

        prizeStockAuditService.log(
            prize.getId(),
            null,
            "ADMIN_UPDATE",
            0,
            prize.getStockLeft(),
            prize.getStockLeft(),
            "管理员更新奖品信息，概率从 " + oldProbability + " 变更为 " + newProbability
        );
        return toPrizeView(prize);
    }

    @Override
    @Transactional
    public PrizeView togglePrize(Long prizeId) {
        Prize prize = prizeRepository.findById(prizeId).orElseThrow(() -> new BusinessException("奖品不存在"));
        prize.setEnabled(!prize.isEnabled());
        prize = prizeRepository.save(prize);
        return toPrizeView(prize);
    }

    @Override
    @Transactional
    public SimpleMessageResponse deletePrize(Long prizeId) {
        Prize prize = prizeRepository.findById(prizeId)
            .orElseThrow(() -> new BusinessException("奖品不存在"));
        if (prize.getStockLeft() > 0) {
            throw new BusinessException("奖品仍有库存，请先确保库存已用完再删除");
        }
        prizeStockAuditService.log(
            prize.getId(),
            null,
            "ADMIN_DELETE",
            0,
            prize.getStockLeft(),
            0,
            "管理员删除奖品"
        );
        prizeItemRepository.deleteByPrizeId(prizeId);
        prizeRepository.deleteById(prizeId);
        return new SimpleMessageResponse("奖品已删除");
    }

    @Override
    public List<PrizeStockAuditView> getStockAudits() {
        Map<Long, String> prizeNameMap = prizeRepository.findAll().stream()
            .collect(Collectors.toMap(Prize::getId, Prize::getName));
        List<PrizeStockAuditView> audits = new ArrayList<>();
        prizeStockAuditRepository.findTop100ByOrderByCreatedAtDesc().forEach(record ->
            audits.add(new PrizeStockAuditView(
                record.getId(),
                record.getPrizeId(),
                prizeNameMap.getOrDefault(record.getPrizeId(), "未知奖品"),
                record.getOperatorUserId(),
                record.getChangeType(),
                record.getDelta(),
                record.getStockBefore(),
                record.getStockAfter(),
                record.getRemark(),
                record.getCreatedAt()
            ))
        );
        return audits;
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
