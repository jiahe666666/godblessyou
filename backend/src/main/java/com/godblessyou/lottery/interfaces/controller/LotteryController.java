package com.godblessyou.lottery.interfaces.controller;

import com.godblessyou.lottery.application.model.DrawResultView;
import com.godblessyou.lottery.application.model.HistoryView;
import com.godblessyou.lottery.application.model.PrizeView;
import com.godblessyou.lottery.application.service.LotteryService;
import com.godblessyou.lottery.infrastructure.security.AuthenticatedUser;
import com.godblessyou.lottery.interfaces.advice.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lottery")
@RequiredArgsConstructor
public class LotteryController {

    private final LotteryService lotteryService;

    @Operation(summary = "获取奖池")
    @GetMapping("/prizes")
    public ApiResponse<List<PrizeView>> getPrizes() {
        return ApiResponse.success(lotteryService.getPrizePool());
    }

    @Operation(summary = "抽奖")
    @PostMapping("/draw")
    public ApiResponse<DrawResultView> draw(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(lotteryService.draw(user.getId()));
    }

    @Operation(summary = "我的中奖记录")
    @GetMapping("/history")
    public ApiResponse<List<HistoryView>> history(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(lotteryService.getHistory(user.getId()));
    }
}
