package com.rks.automation.dto.energy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Per-device energy summary for the Smart Energy Optimization Engine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceEnergySummary {

    private Long deviceId;
    private String deviceName;
    private String deviceType;
    private String location;
    private Double ratedWatt;

    /** kWh consumed in the selected period */
    private double energyKwh;

    /** Runtime in minutes */
    private double runtimeMinutes;

    /** Estimated cost in ₹ */
    private double costInr;

    /** Carbon footprint in kg CO₂ */
    private double carbonKg;

    /** Percentage of total household consumption */
    private double sharePercent;

    /** Whether this device is a peak load contributor */
    private boolean peakContributor;

    /** AI optimization tip for this device */
    private String optimizationTip;
}
