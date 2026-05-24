package com.rks.automation.controller;

import com.rks.automation.common.ApiResponse;
import com.rks.automation.dto.prediction.PredictionsResponse;
import com.rks.automation.service.PredictiveAutomationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI-Based Predictive Automation endpoints.
 *
 * GET  /api/predictions          → get AI suggestions for the current user
 * POST /api/predictions/apply    → apply a suggestion (turn device ON/OFF)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/predictions")
public class PredictiveAutomationController {

    private final PredictiveAutomationService predictionService;

    /**
     * Returns AI-generated automation suggestions based on user behavior patterns.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PredictionsResponse>> getSuggestions(
            @AuthenticationPrincipal UserDetails userDetails) {

        PredictionsResponse response = predictionService.getSuggestions(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    /**
     * Applies a prediction suggestion — controls the device as suggested.
     * Body: { "deviceId": 1, "action": "ON" }
     */
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<String>> applySuggestion(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {

        Long deviceId = Long.valueOf(body.get("deviceId").toString());
        String action = body.get("action").toString();

        predictionService.applySuggestion(userDetails.getUsername(), deviceId, action);
        return ResponseEntity.ok(ApiResponse.success(
                "Suggestion applied successfully", "Device turned " + action));
    }
}
