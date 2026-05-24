package com.rks.automation.dto.prediction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionsResponse {

    /** Suggestions that are actionable right now (current hour matches pattern) */
    private List<PredictionSuggestion> currentSuggestions;

    /** All upcoming suggestions for the rest of the day */
    private List<PredictionSuggestion> upcomingSuggestions;

    /** Total number of behavior events analyzed */
    private long totalEventsAnalyzed;

    /** Days of history used for analysis */
    private int daysAnalyzed;

    private String message;
}
