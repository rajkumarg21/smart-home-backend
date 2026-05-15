package com.rks.automation.dto.deviceAnalytics;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DeviceAnalyticsResponse {
    private Double power;
    private Double duration;
    private LocalDateTime recordedAt;
}
