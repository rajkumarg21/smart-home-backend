package com.rks.automation.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDeviceMetric {

    private Long deviceId;
    private String deviceName;
    private String deviceType;
    private Double runtimeMinutes;
    private Double energyKwh;
}
