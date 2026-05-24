package com.rks.automation.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCommandDecision {

    private String intent;
    private String action;
    private Boolean targetAll;
    private String targetType;
    private String excludeType;
    private String deviceName;
    private String location;
    private String period;
    private String date;
    private String responseHint;
}
