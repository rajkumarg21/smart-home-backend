package com.rks.automation.controller;


import com.rks.automation.dto.deviceAnalytics.DeviceAnalyticsResponse;
import com.rks.automation.common.ApiResponse;
import com.rks.automation.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<ApiResponse<List<DeviceAnalyticsResponse>>> getDeviceAnalytics(
            @PathVariable Long deviceId) {
        List<DeviceAnalyticsResponse> data = analyticsService.getAnalyticsForDevice(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Analytics fetched", data));
    }
}
