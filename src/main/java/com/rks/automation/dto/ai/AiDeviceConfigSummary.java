package com.rks.automation.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDeviceConfigSummary {

    private Long deviceId;
    private String deviceName;
    private String type;
    private String location;
    private String status;
    private String company;
    private Double watt;
    private Integer brightness;
    private Integer speed;
    private Double temperature;
}
