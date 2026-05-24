package com.rks.automation.service.impl;

import com.rks.automation.dto.ai.AiCommandDecision;
import com.rks.automation.dto.ai.AiDeviceCommandResponse;
import com.rks.automation.dto.ai.AiDeviceConfigSummary;
import com.rks.automation.dto.ai.AiDeviceMetric;
import com.rks.automation.dto.device.DeviceResponse;
import com.rks.automation.entity.DeviceAnalytics;
import com.rks.automation.repository.DeviceAnalyticsRepository;
import com.rks.automation.service.AiDeviceControlService;
import com.rks.automation.service.DeviceService;
import com.rks.automation.service.SmartHomeAiAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AiDeviceControlServiceImpl implements AiDeviceControlService {

    private final DeviceService deviceService;
    private final DeviceAnalyticsRepository analyticsRepository;
    private final SmartHomeAiAgent smartHomeAiAgent;

    @Override
    @Transactional
    public AiDeviceCommandResponse executeCommand(String username, String command) {
        String normalizedCommand = normalize(command);
        List<DeviceResponse> devices = deviceService.getDevices(username);

        if (devices.isEmpty()) {
            throw new IllegalArgumentException("No devices are registered for this account.");
        }

        AiCommandDecision aiDecision = smartHomeAiAgent.interpret(command, devices).orElse(null);
        if (aiDecision != null && !isUnknown(aiDecision.getIntent())) {
            return executeAiDecision(username, devices, aiDecision, command);
        }

        if (isAnalyticsQuestion(normalizedCommand)) {
            return answerAnalyticsQuestion(devices, normalizedCommand, command);
        }

        if (isConfigQuestion(normalizedCommand)) {
            return answerConfigQuestion(devices, normalizedCommand, command);
        }

        if (isCountQuestion(normalizedCommand)) {
            return answerDeviceCountQuestion(devices, normalizedCommand, command);
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

    private AiDeviceCommandResponse executeAiDecision(
            String username, List<DeviceResponse> devices, AiCommandDecision decision, String originalCommand) {

        String intent = normalizeToken(decision.getIntent());
        if (intent.contains("ANALYTICS")) {
            return answerAnalyticsQuestion(devices, decision, originalCommand);
        }
        if ("CONFIG_QUERY".equals(intent)) {
            return answerConfigQuestion(devices, decision, originalCommand);
        }
        if ("DEVICE_COUNT".equals(intent)) {
            return answerDeviceCountQuestion(devices, decision, originalCommand);
        }

        if (!"CONTROL".equals(intent)) {
            throw new IllegalArgumentException(decision.getResponseHint() != null
                    ? decision.getResponseHint()
                    : "I could not understand that smart-home request.");
        }

        String action = normalizeToken(decision.getAction());
        if (!action.equals("ON") && !action.equals("OFF")) {
            throw new IllegalArgumentException("Please include whether to turn devices ON or OFF.");
        }

        List<DeviceResponse> targets = findTargets(devices, decision);
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("I could not match that command to any registered device.");
        }

        List<DeviceResponse> updatedDevices = targets.stream()
                .map(device -> deviceService.controlDevice(username, device.getId(), action, "AI_AGENT", originalCommand))
                .toList();

        String message = updatedDevices.size() == 1
                ? updatedDevices.get(0).getName() + " turned " + action
                : updatedDevices.size() + " devices turned " + action;

        return AiDeviceCommandResponse.builder()
                .command(originalCommand)
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

    private List<DeviceResponse> findTargets(List<DeviceResponse> devices, AiCommandDecision decision) {
        String targetType = normalizeToken(decision.getTargetType());
        String excludeType = normalizeToken(decision.getExcludeType());
        String deviceName = normalize(decision.getDeviceName());
        String location = normalize(decision.getLocation());
        boolean targetAll = Boolean.TRUE.equals(decision.getTargetAll());

        if (targetAll || !targetType.isBlank() || !location.isBlank()) {
            return devices.stream()
                    .filter(device -> targetType.isBlank() || targetType.equalsIgnoreCase(device.getType()))
                    .filter(device -> excludeType.isBlank() || !excludeType.equalsIgnoreCase(device.getType()))
                    .filter(device -> location.isBlank() || normalize(device.getLocation()).contains(location.trim())
                            || location.contains(normalize(device.getLocation()).trim()))
                    .toList();
        }

        if (!deviceName.isBlank()) {
            return devices.stream()
                    .filter(device -> normalize(device.getName()).contains(deviceName.trim())
                            || deviceName.contains(normalize(device.getName()).trim()))
                    .toList();
        }

        return List.of();
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

        boolean energyQuestion = containsAny(normalizedCommand, " energy ", " enegy ", " consume ", " comsume ",
                " consumption ", " power ");
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

    private AiDeviceCommandResponse answerConfigQuestion(
            List<DeviceResponse> devices, String normalizedCommand, String originalCommand) {

        List<DeviceResponse> targets = findQuestionTargets(devices, normalizedCommand);
        List<AiDeviceConfigSummary> configs = targets.stream()
                .map(this::toConfigSummary)
                .toList();

        String message = configs.isEmpty()
                ? "No matching devices found for that configuration question."
                : "Found configuration for " + configs.size() + " device" + (configs.size() == 1 ? "." : "s.");

        return AiDeviceCommandResponse.builder()
                .command(originalCommand)
                .intent("CONFIG_QUERY")
                .message(message)
                .totalDeviceCount(configs.size())
                .deviceConfigs(configs)
                .build();
    }

    private AiDeviceCommandResponse answerConfigQuestion(
            List<DeviceResponse> devices, AiCommandDecision decision, String originalCommand) {

        List<DeviceResponse> targets = findAnalyticsTargets(devices, decision);
        List<AiDeviceConfigSummary> configs = targets.stream()
                .map(this::toConfigSummary)
                .toList();

        String message = configs.isEmpty()
                ? "No matching devices found for that configuration question."
                : "Found configuration for " + configs.size() + " device" + (configs.size() == 1 ? "." : "s.");

        return AiDeviceCommandResponse.builder()
                .command(originalCommand)
                .intent("CONFIG_QUERY")
                .message(message)
                .totalDeviceCount(configs.size())
                .deviceConfigs(configs)
                .build();
    }

    private AiDeviceCommandResponse answerDeviceCountQuestion(
            List<DeviceResponse> devices, String normalizedCommand, String originalCommand) {

        List<DeviceResponse> targets = findQuestionTargets(devices, normalizedCommand);
        return buildCountResponse(originalCommand, targets);
    }

    private AiDeviceCommandResponse answerDeviceCountQuestion(
            List<DeviceResponse> devices, AiCommandDecision decision, String originalCommand) {

        List<DeviceResponse> targets = findAnalyticsTargets(devices, decision);
        return buildCountResponse(originalCommand, targets);
    }

    private AiDeviceCommandResponse buildCountResponse(String originalCommand, List<DeviceResponse> targets) {
        int total = targets.size();
        int on = (int) targets.stream().filter(device -> "ON".equalsIgnoreCase(device.getStatus())).count();
        int off = (int) targets.stream().filter(device -> "OFF".equalsIgnoreCase(device.getStatus())).count();

        return AiDeviceCommandResponse.builder()
                .command(originalCommand)
                .intent("DEVICE_COUNT")
                .message("Total devices: " + total + ", ON: " + on + ", OFF: " + off + ".")
                .totalDeviceCount(total)
                .onDeviceCount(on)
                .offDeviceCount(off)
                .deviceConfigs(targets.stream().map(this::toConfigSummary).toList())
                .build();
    }

    private AiDeviceCommandResponse answerAnalyticsQuestion(
            List<DeviceResponse> devices, AiCommandDecision decision, String originalCommand) {

        DateRange range = detectDateRangeFromDecision(decision.getPeriod(), decision.getDate());
        List<DeviceResponse> targets = findAnalyticsTargets(devices, decision);
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

        boolean energyQuestion = "ENERGY_ANALYTICS".equals(normalizeToken(decision.getIntent()));
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
        String location = detectLocation(command);
        if (requestedType != null) {
            return devices.stream()
                    .filter(device -> requestedType.equalsIgnoreCase(device.getType()))
                    .filter(device -> location == null || normalize(device.getLocation()).contains(normalize(location).trim()))
                    .toList();
        }

        if (location != null) {
            return devices.stream()
                    .filter(device -> normalize(device.getLocation()).contains(normalize(location).trim()))
                    .toList();
        }

        if (containsAny(command, " all ", " total ", " whole home ", " every device ", " all device ")) {
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

    private List<DeviceResponse> findQuestionTargets(List<DeviceResponse> devices, String command) {
        String requestedType = detectRequestedType(command);
        String location = detectLocation(command);

        List<DeviceResponse> filtered = devices.stream()
                .filter(device -> requestedType == null || requestedType.equalsIgnoreCase(device.getType()))
                .filter(device -> location == null || normalize(device.getLocation()).contains(normalize(location).trim()))
                .toList();

        if (requestedType != null || location != null || containsAny(command, " all ", " total ", " count ")) {
            return filtered;
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

    private List<DeviceResponse> findAnalyticsTargets(List<DeviceResponse> devices, AiCommandDecision decision) {
        List<DeviceResponse> targets = findTargets(devices, decision);
        if (!targets.isEmpty()) {
            return targets;
        }
        if (Boolean.TRUE.equals(decision.getTargetAll())
                || normalizeToken(decision.getTargetType()).isBlank()
                && normalizeToken(decision.getDeviceName()).isBlank()) {
            return devices;
        }
        return List.of();
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

    private AiDeviceConfigSummary toConfigSummary(DeviceResponse device) {
        return AiDeviceConfigSummary.builder()
                .deviceId(device.getId())
                .deviceName(device.getName())
                .type(device.getType())
                .location(device.getLocation())
                .status(device.getStatus())
                .company(device.getCompany())
                .watt(device.getWatt())
                .brightness(device.getBrightness())
                .speed(device.getSpeed())
                .temperature(device.getTemperature())
                .build();
    }

    private boolean isAnalyticsQuestion(String command) {
        return containsAny(command, " running time ", " runtime ", " run time ", " energy ",
                " enegy ", " consume ", " comsume ", " consumed ", " consumption ", " power usage ");
    }

    private boolean isConfigQuestion(String command) {
        return containsAny(command, " configuration ", " config ", " company ", " brightness ", " britness ",
                " watt ", " speed ", " temperature ", " temprature ");
    }

    private boolean isCountQuestion(String command) {
        return containsAny(command, " count ", " how many ", " total device ", " total devices ",
                " device count ", " on off ");
    }

    private DateRange detectDateRange(String command) {
        LocalDate today = LocalDate.now();
        LocalDate explicitDate = detectExplicitDate(command);
        if (explicitDate != null) {
            return new DateRange(explicitDate.toString(), explicitDate.atStartOfDay(), explicitDate.atTime(LocalTime.MAX));
        }
        if (containsAny(command, " last 1 month ", " last one month ", " last month ", " 1 month ")) {
            return new DateRange("last 1 month", today.minusMonths(1).atStartOfDay(), today.atTime(LocalTime.MAX));
        }
        if (containsAny(command, " last 1 week ", " last one week ", " last week ", " last 1 weak ",
                " last weak ", " 1 week ", " 1 weak ")) {
            return new DateRange("last 1 week", today.minusWeeks(1).atStartOfDay(), today.atTime(LocalTime.MAX));
        }
        return new DateRange("today", today.atStartOfDay(), today.atTime(LocalTime.MAX));
    }

    private DateRange detectDateRangeFromDecision(String period, String date) {
        LocalDate today = LocalDate.now();
        String normalized = normalizeToken(period);
        if ("CUSTOM_DATE".equals(normalized) && date != null && !date.isBlank()) {
            try {
                LocalDate customDate = LocalDate.parse(date.trim());
                return new DateRange(customDate.toString(), customDate.atStartOfDay(), customDate.atTime(LocalTime.MAX));
            } catch (DateTimeParseException ignored) {
                return new DateRange("today", today.atStartOfDay(), today.atTime(LocalTime.MAX));
            }
        }
        if ("LAST_1_MONTH".equals(normalized)) {
            return new DateRange("last 1 month", today.minusMonths(1).atStartOfDay(), today.atTime(LocalTime.MAX));
        }
        if ("LAST_1_WEEK".equals(normalized)) {
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

    private String detectLocation(String command) {
        String[][] aliases = {
                {"hall", " hall "},
                {"kitchen", " kitchen "},
                {"living room", " living room ", " livingroom "},
                {"porch", " porch ", " poarch "},
                {"gallery", " gallery ", " gallary "},
                {"bedroom", " bedroom ", " bed room "},
                {"washroom", " washroom ", " wash room ", " bathroom "}
        };

        for (String[] aliasGroup : aliases) {
            for (int i = 1; i < aliasGroup.length; i++) {
                if (command.contains(aliasGroup[i])) {
                    return aliasGroup[0];
                }
            }
        }
        return null;
    }

    private LocalDate detectExplicitDate(String command) {
        Matcher isoMatcher = Pattern.compile("\\b\\d{4}-\\d{2}-\\d{2}\\b").matcher(command);
        if (isoMatcher.find()) {
            try {
                return LocalDate.parse(isoMatcher.group());
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }

        Matcher slashMatcher = Pattern.compile("\\b(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})\\b").matcher(command);
        if (slashMatcher.find()) {
            try {
                int day = Integer.parseInt(slashMatcher.group(1));
                int month = Integer.parseInt(slashMatcher.group(2));
                int year = Integer.parseInt(slashMatcher.group(3));
                return LocalDate.of(year, month, day);
            } catch (RuntimeException ignored) {
                return null;
            }
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

    private boolean isUnknown(String value) {
        String normalized = normalizeToken(value);
        return normalized.isBlank() || "UNKNOWN".equals(normalized);
    }

    private String normalizeToken(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
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
