package com.rks.automation.service;

import com.rks.automation.dto.energy.EnergyOptimizationResponse;

/**
 * Smart Energy Optimization Engine.
 * Provides device-wise consumption, peak load detection,
 * electricity bill prediction, AI optimization suggestions,
 * and carbon footprint tracking.
 */
public interface EnergyOptimizationService {

    /**
     * Returns the full energy optimization report for the given user and period.
     *
     * @param username  authenticated user
     * @param period    "TODAY", "LAST_7_DAYS", "LAST_30_DAYS"
     * @param tariff    electricity tariff in ₹/kWh (optional, defaults to 10.0)
     */
    EnergyOptimizationResponse getReport(String username, String period, Double tariff);
}
