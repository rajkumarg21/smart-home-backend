package com.rks.automation.controller;

import com.rks.automation.common.ApiResponse;
import com.rks.automation.dto.energy.EnergyOptimizationResponse;
import com.rks.automation.service.EnergyOptimizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Smart Energy Optimization Engine endpoints.
 *
 * GET /api/energy/report?period=TODAY&tariff=10.0
 *   period: TODAY | LAST_7_DAYS | LAST_30_DAYS
 *   tariff: ₹/kWh (optional, default 10.0)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/energy")
public class EnergyOptimizationController {

    private final EnergyOptimizationService energyService;

    @GetMapping("/report")
    public ResponseEntity<ApiResponse<EnergyOptimizationResponse>> getEnergyReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "TODAY") String period,
            @RequestParam(required = false) Double tariff) {

        EnergyOptimizationResponse report = energyService.getReport(
                userDetails.getUsername(), period, tariff);

        return ResponseEntity.ok(ApiResponse.success("Energy report generated", report));
    }
}
