package com.rks.automation.service;

import com.rks.automation.dto.prediction.PredictionsResponse;
import com.rks.automation.entity.Device;
import com.rks.automation.entity.User;

/**
 * AI-Based Predictive Automation Service.
 * Analyzes user behavior logs to predict and suggest device actions.
 */
public interface PredictiveAutomationService {

    /**
     * Returns AI-generated automation suggestions for the given user
     * based on their historical device usage patterns.
     */
    PredictionsResponse getSuggestions(String username);

    /**
     * Records a device control action into the behavior log.
     * Called automatically whenever a device is controlled.
     */
    void recordBehavior(User user, Device device, String action, String source);

    /**
     * Applies a suggested automation — turns the device ON or OFF.
     */
    void applySuggestion(String username, Long deviceId, String action);
}
