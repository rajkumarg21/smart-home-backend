package com.rks.automation.dto.energy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Full Smart Energy Optimization Engine response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyOptimizationResponse {

    // ── Period ────────────────────────────────────────────────────────────────
    private String period;          // "today", "last 7 days", "last 30 days"
    private String startDate;
    private String endDate;

    // ── Totals ────────────────────────────────────────────────────────────────
    private double totalEnergyKwh;
    private double totalCostInr;
    private double totalCarbonKg;
    private double totalRuntimeMinutes;

    // ── Bill Prediction ───────────────────────────────────────────────────────
    /** Projected monthly bill in ₹ based on current usage rate */
    private double projectedMonthlyBillInr;

    /** Projected monthly kWh */
    private double projectedMonthlyKwh;

    // ── Peak Load ─────────────────────────────────────────────────────────────
    /** Hour of day (0-23) with highest energy consumption */
    private int peakLoadHour;
    private String peakLoadHourLabel;
    private double peakLoadKwh;

    // ── Per-device breakdown ──────────────────────────────────────────────────
    private List<DeviceEnergySummary> devices;

    // ── AI Optimization Suggestions ───────────────────────────────────────────
    private List<String> optimizationSuggestions;

    // ── Tariff used ───────────────────────────────────────────────────────────
    /** ₹ per kWh used for cost calculation */
    private double tariffPerKwh;

    /** kg CO₂ per kWh emission factor used */
    private double emissionFactorKgPerKwh;

    // ── Potential savings ─────────────────────────────────────────────────────
    private double potentialSavingsInr;
    private double potentialSavingsKwh;
}
