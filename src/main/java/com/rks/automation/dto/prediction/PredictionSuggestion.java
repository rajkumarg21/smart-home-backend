package com.rks.automation.dto.prediction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single AI-generated automation suggestion based on user behavior patterns.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionSuggestion {

    private Long deviceId;
    private String deviceName;
    private String deviceType;
    private String location;

    /** Suggested action: ON or OFF */
    private String suggestedAction;

    /** Hour at which this action is typically performed (0-23) */
    private int suggestedHour;

    /** Human-readable time label, e.g. "10:30 PM" */
    private String suggestedTimeLabel;

    /** How many times this pattern was observed */
    private long frequency;

    /** Confidence score 0.0 – 1.0 */
    private double confidence;

    /** Human-readable reason, e.g. "You usually turn on Bedroom AC at 10 PM" */
    private String reason;

    /** Whether this suggestion is for the current hour (actionable right now) */
    private boolean actionableNow;
}
