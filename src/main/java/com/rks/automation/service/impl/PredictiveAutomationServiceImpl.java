package com.rks.automation.service.impl;

import com.rks.automation.dto.prediction.PredictionSuggestion;
import com.rks.automation.dto.prediction.PredictionsResponse;
import com.rks.automation.entity.Device;
import com.rks.automation.entity.User;
import com.rks.automation.entity.UserBehaviorLog;
import com.rks.automation.repository.DeviceRepository;
import com.rks.automation.repository.UserBehaviorLogRepository;
import com.rks.automation.repository.UserRepository;
import com.rks.automation.service.PredictiveAutomationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictiveAutomationServiceImpl implements PredictiveAutomationService {

    /** Minimum number of times a pattern must appear to be suggested */
    private static final int MIN_FREQUENCY = 2;

    /** Minimum confidence (0.0–1.0) to include a suggestion */
    private static final double MIN_CONFIDENCE = 0.3;

    /** Number of days of history to analyze */
    private static final int ANALYSIS_DAYS = 30;

    private final UserBehaviorLogRepository behaviorLogRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    // ── Public API ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PredictionsResponse getSuggestions(String username) {
        User user = resolveUser(username);
        LocalDateTime since = LocalDateTime.now().minusDays(ANALYSIS_DAYS);

        // Raw pattern rows: [deviceId, action, hourOfDay, frequency]
        List<Object[]> patterns = behaviorLogRepository.findAllPatterns(user.getId(), since);

        if (patterns.isEmpty()) {
            return PredictionsResponse.builder()
                    .currentSuggestions(List.of())
                    .upcomingSuggestions(List.of())
                    .totalEventsAnalyzed(0)
                    .daysAnalyzed(ANALYSIS_DAYS)
                    .message("Not enough data yet. Keep using your devices and predictions will appear here.")
                    .build();
        }

        // Build device lookup map
        List<Device> devices = deviceRepository.findByUserId(user.getId());
        Map<Long, Device> deviceMap = devices.stream()
                .collect(Collectors.toMap(Device::getId, d -> d));

        // Calculate max frequency per device+action for confidence normalization
        Map<String, Long> maxFreqByDeviceAction = new HashMap<>();
        for (Object[] row : patterns) {
            Long deviceId = ((Number) row[0]).longValue();
            String action = (String) row[1];
            long freq = ((Number) row[3]).longValue();
            String key = deviceId + "_" + action;
            maxFreqByDeviceAction.merge(key, freq, Math::max);
        }

        long totalEvents = patterns.stream()
                .mapToLong(row -> ((Number) row[3]).longValue())
                .sum();

        int currentHour = LocalDateTime.now().getHour();
        List<PredictionSuggestion> allSuggestions = new ArrayList<>();

        for (Object[] row : patterns) {
            Long deviceId = ((Number) row[0]).longValue();
            String action = (String) row[1];
            int hour = ((Number) row[2]).intValue();
            long freq = ((Number) row[3]).longValue();

            if (freq < MIN_FREQUENCY) continue;

            Device device = deviceMap.get(deviceId);
            if (device == null) continue;

            String key = deviceId + "_" + action;
            long maxFreq = maxFreqByDeviceAction.getOrDefault(key, 1L);
            double confidence = Math.min(1.0, (double) freq / Math.max(maxFreq, 1));

            if (confidence < MIN_CONFIDENCE) continue;

            String timeLabel = formatHour(hour);
            String reason = buildReason(device, action, hour, freq, timeLabel);

            allSuggestions.add(PredictionSuggestion.builder()
                    .deviceId(deviceId)
                    .deviceName(device.getName())
                    .deviceType(device.getType())
                    .location(device.getLocation())
                    .suggestedAction(action)
                    .suggestedHour(hour)
                    .suggestedTimeLabel(timeLabel)
                    .frequency(freq)
                    .confidence(Math.round(confidence * 100.0) / 100.0)
                    .reason(reason)
                    .actionableNow(hour == currentHour)
                    .build());
        }

        // Sort: actionable now first, then by confidence desc
        allSuggestions.sort(Comparator
                .comparing(PredictionSuggestion::isActionableNow).reversed()
                .thenComparingDouble(PredictionSuggestion::getConfidence).reversed());

        List<PredictionSuggestion> current = allSuggestions.stream()
                .filter(PredictionSuggestion::isActionableNow)
                .toList();

        List<PredictionSuggestion> upcoming = allSuggestions.stream()
                .filter(s -> !s.isActionableNow() && s.getSuggestedHour() > currentHour)
                .sorted(Comparator.comparingInt(PredictionSuggestion::getSuggestedHour))
                .toList();

        String message = current.isEmpty()
                ? "No immediate suggestions. Check upcoming predictions below."
                : current.size() + " suggestion" + (current.size() > 1 ? "s" : "") + " ready for you right now.";

        return PredictionsResponse.builder()
                .currentSuggestions(current)
                .upcomingSuggestions(upcoming)
                .totalEventsAnalyzed(totalEvents)
                .daysAnalyzed(ANALYSIS_DAYS)
                .message(message)
                .build();
    }

    @Override
    @Transactional
    public void recordBehavior(User user, Device device, String action, String source) {
        try {
            LocalDateTime now = LocalDateTime.now();
            UserBehaviorLog log = UserBehaviorLog.builder()
                    .user(user)
                    .device(device)
                    .action(action.toUpperCase())
                    .source(source)
                    .hourOfDay(now.getHour())
                    .dayOfWeek(now.getDayOfWeek().getValue())
                    .recordedAt(now)
                    .build();
            behaviorLogRepository.save(log);
        } catch (Exception ex) {
            // Non-critical — log and continue
            log.warn("Failed to record behavior log for device {}: {}", device.getId(), ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void applySuggestion(String username, Long deviceId, String action) {
        User user = resolveUser(username);
        Device device = deviceRepository.findByIdAndUserId(deviceId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));

        String normalized = action.toUpperCase();
        if (!normalized.equals("ON") && !normalized.equals("OFF")) {
            throw new IllegalArgumentException("Invalid action: " + action);
        }

        device.setStatus(normalized);
        deviceRepository.save(device);
        recordBehavior(user, device, normalized, "AI_PREDICTION");
        log.info("Applied AI prediction: {} → {} for user {}", device.getName(), normalized, username);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String buildReason(Device device, String action, int hour, long freq, String timeLabel) {
        String actionWord = "ON".equals(action) ? "turn on" : "turn off";
        String location = device.getLocation() != null && !device.getLocation().isBlank()
                ? " in " + device.getLocation()
                : "";
        return String.format("You usually %s %s%s around %s (%d times in last %d days)",
                actionWord, device.getName(), location, timeLabel, freq, ANALYSIS_DAYS);
    }

    private String formatHour(int hour) {
        if (hour == 0) return "12:00 AM";
        if (hour == 12) return "12:00 PM";
        if (hour < 12) return hour + ":00 AM";
        return (hour - 12) + ":00 PM";
    }

    private User resolveUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
