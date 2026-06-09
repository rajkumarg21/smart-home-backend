package com.rks.automation.service.impl;

import com.rks.automation.dto.ai.AiDeviceMetric;
import com.rks.automation.dto.ai.AiUsageNotification;
import com.rks.automation.dto.ai.AiUsageReportResponse;
import com.rks.automation.dto.device.DeviceResponse;
import com.rks.automation.entity.DeviceAnalytics;
import com.rks.automation.repository.DeviceAnalyticsRepository;
import com.rks.automation.service.AiUsageReportService;
import com.rks.automation.service.DeviceService;
import com.rks.automation.service.SmartHomeAiAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiUsageReportServiceImpl implements AiUsageReportService {

    private final DeviceService deviceService;
    private final DeviceAnalyticsRepository analyticsRepository;
    private final SmartHomeAiAgent smartHomeAiAgent;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(readOnly = true)
    public AiUsageReportResponse generateReport(String username, String period, boolean notifyUser) {
        List<DeviceResponse> devices = deviceService.getDevices(username);
        DateRange range = resolveRange(period);

        List<DeviceAnalytics> analytics = devices.isEmpty()
                ? List.of()
                : analyticsRepository.findByDeviceUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(
                        devices.get(0).getUserId(), range.start(), range.end());

        Map<Long, DeviceResponse> devicesById = devices.stream()
                .collect(Collectors.toMap(DeviceResponse::getId, device -> device));

        List<AiDeviceMetric> summaries = analytics.stream()
                .collect(Collectors.groupingBy(row -> row.getDevice().getId()))
                .entrySet()
                .stream()
                .map(entry -> toMetric(devicesById.get(entry.getKey()), entry.getValue()))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(AiDeviceMetric::getEnergyKwh).reversed())
                .toList();

        double totalRuntime = round2(summaries.stream()
                .mapToDouble(metric -> safe(metric.getRuntimeMinutes()))
                .sum());
        double totalEnergy = round3(summaries.stream()
                .mapToDouble(metric -> safe(metric.getEnergyKwh()))
                .sum());

        AiUsageReportResponse draft = AiUsageReportResponse.builder()
                .period(range.label())
                .start(range.start())
                .end(range.end())
                .totalRuntimeMinutes(totalRuntime)
                .totalEnergyKwh(totalEnergy)
                .totalDevices(devices.size())
                .activeDevices(summaries.size())
                .deviceSummaries(summaries)
                .build();

        String report = smartHomeAiAgent.generateUsageReport(draft)
                .orElseGet(() -> buildFallbackReport(draft));
        draft.setReport(report);

        if (notifyUser) {
            publishNotification(username, draft);
        }

        return draft;
    }

    private AiDeviceMetric toMetric(DeviceResponse device, List<DeviceAnalytics> rows) {
        if (device == null) {
            return null;
        }

        double runtime = rows.stream()
                .mapToDouble(row -> safe(row.getDuration()))
                .sum();
        double energy = rows.stream()
                .mapToDouble(row -> safe(row.getPower()) * safe(row.getDuration()) / 60.0 / 1000.0)
                .sum();

        return AiDeviceMetric.builder()
                .deviceId(device.getId())
                .deviceName(device.getName())
                .deviceType(device.getType())
                .runtimeMinutes(round2(runtime))
                .energyKwh(round3(energy))
                .build();
    }

    private String buildFallbackReport(AiUsageReportResponse report) {
        if (report.getDeviceSummaries() == null || report.getDeviceSummaries().isEmpty()) {
            return "No usage activity was recorded for " + report.getPeriod()
                    + ". Once devices report runtime data, this report will highlight energy use and savings tips.";
        }

        AiDeviceMetric topDevice = report.getDeviceSummaries().get(0);
        return "Usage report for " + report.getPeriod() + ": "
                + report.getActiveDevices() + " of " + report.getTotalDevices() + " devices reported activity. "
                + "Total runtime was " + report.getTotalRuntimeMinutes() + " minutes and estimated energy use was "
                + report.getTotalEnergyKwh() + " kWh. Highest usage came from " + topDevice.getDeviceName()
                + " at " + topDevice.getEnergyKwh() + " kWh. Consider scheduling high-use devices during needed hours only.";
    }

    private void publishNotification(String username, AiUsageReportResponse report) {
        AiUsageNotification notification = AiUsageNotification.builder()
                .type("AI_USAGE_REPORT")
                .title("AI usage report ready")
                .message(report.getReport())
                .report(report)
                .build();

        messagingTemplate.convertAndSend("/topic/users/" + username + "/notifications", notification);
    }

    private DateRange resolveRange(String period) {
        LocalDate today = LocalDate.now();
        String normalized = period == null ? "TODAY" : period.trim().toUpperCase(Locale.ROOT);
        if ("LAST_1_MONTH".equals(normalized) || "MONTH".equals(normalized)) {
            return new DateRange("last 1 month", today.minusMonths(1).atStartOfDay(), today.atTime(LocalTime.MAX));
        }
        if ("LAST_1_WEEK".equals(normalized) || "WEEK".equals(normalized)) {
            return new DateRange("last 1 week", today.minusWeeks(1).atStartOfDay(), today.atTime(LocalTime.MAX));
        }
        return new DateRange("today", today.atStartOfDay(), today.atTime(LocalTime.MAX));
    }

    private double safe(Double value) {
        return value == null ? 0.0 : value;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private record DateRange(String label, LocalDateTime start, LocalDateTime end) {
    }
}
