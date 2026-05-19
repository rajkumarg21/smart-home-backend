package com.rks.automation.service.impl;

import com.rks.automation.dto.ai.AiDeviceCommandResponse;
import com.rks.automation.dto.ai.AiDeviceMetric;
import com.rks.automation.dto.device.DeviceResponse;
import com.rks.automation.entity.DeviceAnalytics;
import com.rks.automation.repository.DeviceAnalyticsRepository;
import com.rks.automation.service.AiDeviceControlService;
import com.rks.automation.service.DeviceService;
import lombok.RequiredArgsConstructor;
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
public class AiDeviceControlServiceImpl implements AiDeviceControlService {

    private final DeviceService deviceService;
    private final DeviceAnalyticsRepository analyticsRepository;

    @Override
    @Transactional
    public AiDeviceCommandResponse executeCommand(String username, String command) {
        String normalizedCommand = normalize(command);
        List<DeviceResponse> devices = deviceService.getDevices(username);

        if (devices.isEmpty()) {
            throw new IllegalArgumentException("No devices are registered for this account.");
        }

        if (isAnalyticsQuestion(normalizedCommand)) {
            return answerAnalyticsQuestion(devices, normalizedCommand, command);
        }

        String action = detectAction(normalizedCommand);
        List<DeviceResponse> targets = findTargets(devices, normalizedCommand);
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("I could not match that command to any registered device.");
        }

        List<DeviceResponse> updatedDevices = targets.stream()
                .map(device -> deviceService.controlDevice(username, device.getId(), action, "AI", command))
                .toList();

        String message = updatedDevices.size() == 1
                ? updatedDevices.get(0).getName() + " turned " + action
                : updatedDevices.size() + " devices turned " + action;

        return AiDeviceCommandResponse.builder()
                .command(command)
                .intent("CONTROL")
                .action(action)
                .message(message)
                .affectedDevices(updatedDevices)
                .build();
    }

    private String detectAction(String command) {
        if (containsAny(command, "turn on", "switch on", "power on", "start", "enable", "open", "unlock")) {
            return "ON";
        }
        if (containsAny(command, "turn off", "switch off", "power off", "stop", "disable", "close", "lock")) {
            return "OFF";
        }
        throw new IllegalArgumentException("Please include an action such as turn on, turn off, open, close, lock, or unlock.");
    }

    private List<DeviceResponse> findTargets(List<DeviceResponse> devices, String command) {
        boolean allDevices = containsAny(command, " all ", " everything ", " every device ", " whole home ");
        boolean hasExclusion = containsAny(command, " except ", " acept ", " excluding ", " without ");
        String excludedType = detectExcludedType(command);
        String requestedType = detectRequestedType(command);

        if (allDevices) {
            return devices.stream()
                    .filter(device -> hasExclusion || requestedType == null || requestedType.equalsIgnoreCase(device.getType()))
                    .filter(device -> excludedType == null || !excludedType.equalsIgnoreCase(device.getType()))
                    .toList();
        }

        if (requestedType != null && containsAny(command, " all " + requestedType.toLowerCase(Locale.ROOT) + " ",
                " every " + requestedType.toLowerCase(Locale.ROOT) + " ")) {
            return devices.stream()
                    .filter(device -> requestedType.equalsIgnoreCase(device.getType()))
                    .toList();
        }

        int bestScore = devices.stream()
                .map(device -> matchScore(device, command))
                .max(Comparator.naturalOrder())
                .orElse(0);

        if (bestScore <= 0) {
            return List.of();
        }

        return devices.stream()
                .filter(device -> matchScore(device, command) == bestScore)
                .toList();
    }

    private int matchScore(DeviceResponse device, String command) {
        int score = 0;
        String name = normalize(device.getName());
        String type = normalize(device.getType());
        String location = normalize(device.getLocation());

        if (!name.isBlank() && command.contains(name)) {
            score += 8;
        }
        if (!type.isBlank() && command.contains(type)) {
            score += 4;
        }
        if (!location.isBlank() && command.contains(location)) {
            score += 4;
        }
        if (!location.isBlank() && !type.isBlank() && command.contains(location + " " + type)) {
            score += 6;
        }
        return score;
    }

    private AiDeviceCommandResponse answerAnalyticsQuestion(
            List<DeviceResponse> devices, String normalizedCommand, String originalCommand) {

        DateRange range = detectDateRange(normalizedCommand);
        List<DeviceResponse> targets = findAnalyticsTargets(devices, normalizedCommand);
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("I could not match that analytics question to any registered device.");
        }

        Long userId = devices.get(0).getUserId();
        List<DeviceAnalytics> analytics = analyticsRepository
                .findByDeviceUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(userId, range.start(), range.end())
                .stream()
                .filter(row -> targets.stream().anyMatch(device -> Objects.equals(device.getId(), row.getDevice().getId())))
                .toList();

        Map<Long, DeviceResponse> devicesById = devices.stream()
                .collect(Collectors.toMap(DeviceResponse::getId, device -> device));

        List<AiDeviceMetric> metrics = analytics.stream()
                .collect(Collectors.groupingBy(row -> row.getDevice().getId()))
                .entrySet()
                .stream()
                .map(entry -> toMetric(devicesById.get(entry.getKey()), entry.getValue()))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(AiDeviceMetric::getDeviceName))
                .toList();

        double totalRuntime = round2(metrics.stream()
                .mapToDouble(metric -> safe(metric.getRuntimeMinutes()))
                .sum());
        double totalEnergy = round3(metrics.stream()
                .mapToDouble(metric -> safe(metric.getEnergyKwh()))
                .sum());

        boolean energyQuestion = containsAny(normalizedCommand, " energy ", " consume ", " consumption ", " power ");
        String targetLabel = describeTargets(targets, devices.size());
        String message = energyQuestion
                ? targetLabel + " consumed " + totalEnergy + " kWh for " + range.label() + "."
                : targetLabel + " ran for " + totalRuntime + " minutes for " + range.label() + ".";

        return AiDeviceCommandResponse.builder()
                .command(originalCommand)
                .intent(energyQuestion ? "ENERGY_ANALYTICS" : "RUNTIME_ANALYTICS")
                .message(message)
                .period(range.label())
                .totalRuntimeMinutes(totalRuntime)
                .totalEnergyKwh(totalEnergy)
                .metrics(metrics)
                .build();
    }

    private List<DeviceResponse> findAnalyticsTargets(List<DeviceResponse> devices, String command) {
        String requestedType = detectRequestedType(command);
        if (requestedType != null) {
            return devices.stream()
                    .filter(device -> requestedType.equalsIgnoreCase(device.getType()))
                    .toList();
        }

        if (containsAny(command, " all ", " total ", " whole home ", " every device ")) {
            return devices;
        }

        int bestScore = devices.stream()
                .map(device -> matchScore(device, command))
                .max(Comparator.naturalOrder())
                .orElse(0);

        if (bestScore <= 0) {
            return devices;
        }

        return devices.stream()
                .filter(device -> matchScore(device, command) == bestScore)
                .toList();
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

    private boolean isAnalyticsQuestion(String command) {
        return containsAny(command, " running time ", " runtime ", " run time ", " energy ",
                " enegy ", " consume ", " comsume ", " consumed ", " consumption ", " power usage ");
    }

    private DateRange detectDateRange(String command) {
        LocalDate today = LocalDate.now();
        if (containsAny(command, " last 1 month ", " last one month ", " last month ", " 1 month ")) {
            return new DateRange("last 1 month", today.minusMonths(1).atStartOfDay(), today.atTime(LocalTime.MAX));
        }
        if (containsAny(command, " last 1 week ", " last one week ", " last week ", " last 1 weak ",
                " last weak ", " 1 week ", " 1 weak ")) {
            return new DateRange("last 1 week", today.minusWeeks(1).atStartOfDay(), today.atTime(LocalTime.MAX));
        }
        return new DateRange("today", today.atStartOfDay(), today.atTime(LocalTime.MAX));
    }

    private String detectRequestedType(String command) {
        if (containsAny(command, " light ", " lights ")) {
            return "LIGHT";
        }
        if (containsAny(command, " fan ", " fans ")) {
            return "FAN";
        }
        if (containsAny(command, " ac ", " air conditioner ", " air conditioners ")) {
            return "AC";
        }
        if (containsAny(command, " lock ", " locks ", " door ")) {
            return "LOCK";
        }
        if (containsAny(command, " camera ", " cameras ")) {
            return "CAMERA";
        }
        return null;
    }

    private String detectExcludedType(String command) {
        if (!containsAny(command, " except ", " acept ", " excluding ", " without ")) {
            return null;
        }
        String afterExclusion = command.substring(Math.max(
                Math.max(Math.max(command.indexOf(" except "), command.indexOf(" acept ")), command.indexOf(" excluding ")),
                command.indexOf(" without ")));
        return detectRequestedType(afterExclusion);
    }

    private String describeTargets(List<DeviceResponse> targets, int totalDeviceCount) {
        if (targets.size() == totalDeviceCount) {
            return "All devices";
        }
        if (targets.size() == 1) {
            return targets.get(0).getName();
        }

        String type = targets.get(0).getType();
        boolean sameType = targets.stream().allMatch(device -> type.equalsIgnoreCase(device.getType()));
        if (sameType) {
            return "All " + type.toLowerCase(Locale.ROOT) + " devices";
        }
        return targets.size() + " devices";
    }

    private boolean containsAny(String value, String... phrases) {
        for (String phrase : phrases) {
            if (value.contains(phrase)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return " " + value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim() + " ";
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
