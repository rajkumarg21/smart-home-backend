package com.rks.automation.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiUsageReportResponse {

    private String period;
    private LocalDateTime start;
    private LocalDateTime end;
    private Double totalRuntimeMinutes;
    private Double totalEnergyKwh;
    private Integer totalDevices;
    private Integer activeDevices;
    private String report;
    private List<AiDeviceMetric> deviceSummaries;
}
