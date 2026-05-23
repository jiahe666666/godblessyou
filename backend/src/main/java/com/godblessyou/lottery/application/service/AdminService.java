package com.godblessyou.lottery.application.service;

import com.godblessyou.lottery.application.model.DashboardView;
import com.godblessyou.lottery.application.model.PrizeView;
import com.godblessyou.lottery.application.model.PrizeStockAuditView;
import com.godblessyou.lottery.application.model.SimpleMessageResponse;
import com.godblessyou.lottery.interfaces.dto.admin.PrizeCreateRequest;
import com.godblessyou.lottery.interfaces.dto.admin.PrizeUpdateRequest;
import java.util.List;

public interface AdminService {

    DashboardView getDashboard();

    List<PrizeView> getPrizes();

    List<PrizeStockAuditView> getStockAudits();

    PrizeView createPrize(PrizeCreateRequest request);

    PrizeView updatePrize(Long prizeId, PrizeUpdateRequest request);

    PrizeView togglePrize(Long prizeId);

    SimpleMessageResponse deletePrize(Long prizeId);
}
