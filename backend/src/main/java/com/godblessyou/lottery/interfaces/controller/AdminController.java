package com.godblessyou.lottery.interfaces.controller;

import com.godblessyou.lottery.application.model.DashboardView;
import com.godblessyou.lottery.application.model.PrizeView;
import com.godblessyou.lottery.application.model.PrizeStockAuditView;
import com.godblessyou.lottery.application.model.SimpleMessageResponse;
import com.godblessyou.lottery.application.service.AdminService;
import com.godblessyou.lottery.interfaces.advice.ApiResponse;
import com.godblessyou.lottery.interfaces.dto.admin.PrizeCreateRequest;
import com.godblessyou.lottery.interfaces.dto.admin.PrizeUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "仪表盘统计")
    @GetMapping("/dashboard")
    public ApiResponse<DashboardView> dashboard() {
        return ApiResponse.success(adminService.getDashboard());
    }

    @Operation(summary = "奖品列表")
    @GetMapping("/prizes")
    public ApiResponse<List<PrizeView>> prizes() {
        return ApiResponse.success(adminService.getPrizes());
    }

    @Operation(summary = "库存审计日志")
    @GetMapping("/stock-audits")
    public ApiResponse<List<PrizeStockAuditView>> stockAudits() {
        return ApiResponse.success(adminService.getStockAudits());
    }

    @Operation(summary = "添加奖品")
    @PostMapping("/prizes")
    public ApiResponse<PrizeView> createPrize(@Valid @RequestBody PrizeCreateRequest request) {
        return ApiResponse.success(adminService.createPrize(request));
    }

    @Operation(summary = "编辑奖品")
    @PutMapping("/prizes/{id}")
    public ApiResponse<PrizeView> updatePrize(@PathVariable Long id, @Valid @RequestBody PrizeUpdateRequest request) {
        return ApiResponse.success(adminService.updatePrize(id, request));
    }

    @Operation(summary = "启用禁用奖品")
    @PutMapping("/prizes/{id}/toggle")
    public ApiResponse<PrizeView> toggle(@PathVariable Long id) {
        return ApiResponse.success(adminService.togglePrize(id));
    }

    @Operation(summary = "删除奖品")
    @DeleteMapping("/prizes/{id}")
    public ApiResponse<SimpleMessageResponse> deletePrize(@PathVariable Long id) {
        return ApiResponse.success(adminService.deletePrize(id));
    }
}
