package com.rks.automation.dto.ai;

import java.util.List;

import com.rks.automation.dto.device.DeviceResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDeviceCommandResponse {

    private String command;
    private String intent;
    private String action;
    private String message;
    private String period;
    private Double totalRuntimeMinutes;
    private Double totalEnergyKwh;
    private List<DeviceResponse> affectedDevices;
    private List<AiDeviceMetric> metrics;
}
