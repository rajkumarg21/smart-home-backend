package com.rks.automation.service.impl;

import com.rks.automation.dto.energy.DeviceEnergySummary;
import com.rks.automation.dto.energy.EnergyOptimizationResponse;
import com.rks.automation.entity.DeviceAnalytics;
import com.rks.automation.repository.DeviceAnalyticsRepository;
import com.rks.automation.repository.UserRepository;
import com.rks.automation.service.EnergyOptimizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnergyOptimizationServiceImpl implements EnergyOptimizationService {

    /** Default electricity tariff ₹/kWh (India average) */
    private static final double DEFAULT_TARIFF = 10.0;

    /** India grid emission factor: kg CO₂ per kWh */
    private static final double EMISSION_FACTOR = 0.82;

    /** Devices consuming more than this share of total are "peak contributors" */
    private static final double PEAK_THRESHOLD_PERCENT = 25.0;

    private final DeviceAnalyticsRepository analyticsRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public EnergyOptimizationResponse getReport(String username, String period, Double tariff) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        double effectiveTariff = (tariff != null && tariff > 0) ? tariff : DEFAULT_TARIFF;
        DateRange range = resolveDateRange(period);

        List<DeviceAnalytics> records = analyticsRepository
                .findByDeviceUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(
                        user.getId(), range.start(), range.end());

        if (records.isEmpty()) {
            return EnergyOptimizationResponse.builder()
                    .period(range.label())
                    .startDate(range.start().toLocalDate().toString())
                    .endDate(range.end().toLocalDate().toString())
                    .totalEnergyKwh(0)
                    .totalCostInr(0)
                    .totalCarbonKg(0)
                    .totalRuntimeMinutes(0)
                    .projectedMonthlyBillInr(0)
                    .projectedMonthlyKwh(0)
                    .peakLoadHour(0)
                    .peakLoadHourLabel("N/A")
                    .peakLoadKwh(0)
                    .devices(List.of())
                    .optimizationSuggestions(List.of("No usage data found for this period. Start using your devices to see energy insights."))
                    .tariffPerKwh(effectiveTariff)
                    .emissionFactorKgPerKwh(EMISSION_FACTOR)
                    .potentialSavingsInr(0)
                    .potentialSavingsKwh(0)
                    .build();
        }

        // ── Per-device aggregation ────────────────────────────────────────────
        Map<Long, List<DeviceAnalytics>> byDevice = records.stream()
                .collect(Collectors.groupingBy(r -> r.getDevice().getId()));

        double totalKwh = 0;
        double totalMinutes = 0;
        List<DeviceEnergySummary> deviceSummaries = new ArrayList<>();

        for (Map.Entry<Long, List<DeviceAnalytics>> entry : byDevice.entrySet()) {
            var deviceRecords = entry.getValue();
            var device = deviceRecords.get(0).getDevice();

            double kwh = deviceRecords.stream()
                    .mapToDouble(r -> safe(r.getPower()) * safe(r.getDuration()) / 60.0 / 1000.0)
                    .sum();
            double minutes = deviceRecords.stream()
                    .mapToDouble(r -> safe(r.getDuration()))
                    .sum();

            totalKwh += kwh;
            totalMinutes += minutes;

            deviceSummaries.add(DeviceEnergySummary.builder()
                    .deviceId(device.getId())
                    .deviceName(device.getName())
                    .deviceType(device.getType())
                    .location(device.getLocation())
                    .ratedWatt(device.getWatt())
                    .energyKwh(round3(kwh))
                    .runtimeMinutes(round2(minutes))
                    .costInr(round2(kwh * effectiveTariff))
                    .carbonKg(round3(kwh * EMISSION_FACTOR))
                    .sharePercent(0) // filled below
                    .peakContributor(false) // filled below
                    .optimizationTip("") // filled below
                    .build());
        }

        double finalTotalKwh = totalKwh;

        // ── Share % and peak contributor ──────────────────────────────────────
        deviceSummaries.forEach(d -> {
            double share = finalTotalKwh > 0 ? (d.getEnergyKwh() / finalTotalKwh) * 100.0 : 0;
            d.setSharePercent(round2(share));
            d.setPeakContributor(share >= PEAK_THRESHOLD_PERCENT);
            d.setOptimizationTip(generateDeviceTip(d));
        });

        // Sort by energy desc
        deviceSummaries.sort(Comparator.comparingDouble(DeviceEnergySummary::getEnergyKwh).reversed());

        // ── Peak load hour ────────────────────────────────────────────────────
        Map<Integer, Double> kwhByHour = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getRecordedAt().getHour(),
                        Collectors.summingDouble(r -> safe(r.getPower()) * safe(r.getDuration()) / 60.0 / 1000.0)));

        int peakHour = kwhByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
        double peakKwh = kwhByHour.getOrDefault(peakHour, 0.0);

        // ── Totals ────────────────────────────────────────────────────────────
        double totalCost = round2(totalKwh * effectiveTariff);
        double totalCarbon = round3(totalKwh * EMISSION_FACTOR);

        // ── Bill projection ───────────────────────────────────────────────────
        long periodDays = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(
                range.start().toLocalDate(), range.end().toLocalDate()) + 1);
        double dailyKwh = totalKwh / periodDays;
        double projectedMonthlyKwh = round2(dailyKwh * 30);
        double projectedMonthlyBill = round2(projectedMonthlyKwh * effectiveTariff);

        // ── AI optimization suggestions ───────────────────────────────────────
        List<String> suggestions = generateOptimizationSuggestions(
                deviceSummaries, totalKwh, peakHour, projectedMonthlyBill, effectiveTariff);

        // ── Potential savings ─────────────────────────────────────────────────
        double potentialSavingsPercent = 0.20; // 20% savings potential
        double potentialSavingsKwh = round2(totalKwh * potentialSavingsPercent);
        double potentialSavingsInr = round2(potentialSavingsKwh * effectiveTariff);

        return EnergyOptimizationResponse.builder()
                .period(range.label())
                .startDate(range.start().toLocalDate().toString())
                .endDate(range.end().toLocalDate().toString())
                .totalEnergyKwh(round3(totalKwh))
                .totalCostInr(totalCost)
                .totalCarbonKg(totalCarbon)
                .totalRuntimeMinutes(round2(totalMinutes))
                .projectedMonthlyBillInr(projectedMonthlyBill)
                .projectedMonthlyKwh(projectedMonthlyKwh)
                .peakLoadHour(peakHour)
                .peakLoadHourLabel(formatHour(peakHour))
                .peakLoadKwh(round3(peakKwh))
                .devices(deviceSummaries)
                .optimizationSuggestions(suggestions)
                .tariffPerKwh(effectiveTariff)
                .emissionFactorKgPerKwh(EMISSION_FACTOR)
                .potentialSavingsInr(potentialSavingsInr)
                .potentialSavingsKwh(potentialSavingsKwh)
                .build();
    }

    // ── Tip generators ────────────────────────────────────────────────────────

    private String generateDeviceTip(DeviceEnergySummary d) {
        String type = d.getDeviceType() != null ? d.getDeviceType().toUpperCase() : "";
        double hours = d.getRuntimeMinutes() / 60.0;

        return switch (type) {
            case "AC" -> {
                if (hours > 8) yield "AC running " + round1(hours) + "h/day. Set to 24°C and use sleep mode to save ~30%.";
                yield "Consider using AC timer to auto-off after sleeping.";
            }
            case "LIGHT" -> {
                if (d.getRatedWatt() != null && d.getRatedWatt() > 20)
                    yield "Switch to LED bulbs to reduce lighting energy by up to 80%.";
                yield "Turn off lights when leaving the room.";
            }
            case "FAN" -> {
                if (hours > 12) yield "Fan running " + round1(hours) + "h/day. Use timer to reduce idle runtime.";
                yield "Fans use minimal energy — keep using them instead of AC when possible.";
            }
            case "LOCK" -> "Smart locks use very little energy. No action needed.";
            case "CAMERA" -> {
                if (hours > 20) yield "Camera running 24/7. Consider motion-triggered recording to save energy.";
                yield "Camera energy usage is minimal.";
            }
            default -> {
                if (d.getSharePercent() > 20)
                    yield "This device uses " + round1(d.getSharePercent()) + "% of your total energy. Review usage schedule.";
                yield "Monitor usage patterns to identify optimization opportunities.";
            }
        };
    }

    private List<String> generateOptimizationSuggestions(
            List<DeviceEnergySummary> devices, double totalKwh,
            int peakHour, double projectedBill, double tariff) {

        List<String> tips = new ArrayList<>();

        // Top consumer tip
        if (!devices.isEmpty()) {
            DeviceEnergySummary top = devices.get(0);
            tips.add(String.format("🔋 %s is your top energy consumer (%.2f kWh, ₹%.0f). %s",
                    top.getDeviceName(), top.getEnergyKwh(), top.getCostInr(), top.getOptimizationTip()));
        }

        // Peak load tip
        tips.add(String.format("⚡ Peak usage detected at %s. Shift heavy appliances to off-peak hours (10 PM–6 AM) to reduce load.",
                formatHour(peakHour)));

        // Bill prediction tip
        if (projectedBill > 500) {
            tips.add(String.format("💰 Projected monthly bill: ₹%.0f. Reducing AC usage by 1 hour/day could save ~₹%.0f/month.",
                    projectedBill, tariff * 1.5 * 30));
        }

        // AC-specific tip
        devices.stream()
                .filter(d -> "AC".equalsIgnoreCase(d.getDeviceType()) && d.getRuntimeMinutes() > 300)
                .findFirst()
                .ifPresent(ac -> tips.add(String.format(
                        "❄️ Set AC temperature to 24°C instead of 18°C to save up to 24%% energy (saves ~₹%.0f/month).",
                        ac.getCostInr() * 0.24 * 30)));

        // Carbon footprint tip
        double carbonKg = totalKwh * 0.82;
        if (carbonKg > 5) {
            tips.add(String.format("🌱 Your carbon footprint is %.2f kg CO₂. Reducing by 20%% equals planting %.0f trees/year.",
                    carbonKg, carbonKg * 0.2 / 0.021));
        }

        // Standby power tip
        long alwaysOnCount = devices.stream()
                .filter(d -> d.getRuntimeMinutes() > 1380) // >23 hours
                .count();
        if (alwaysOnCount > 0) {
            tips.add(String.format("🔌 %d device(s) running nearly 24/7. Use smart plugs with schedules to eliminate standby waste.",
                    alwaysOnCount));
        }

        // Solar suggestion
        if (projectedBill > 1000) {
            tips.add("☀️ Your usage qualifies for solar panel ROI in under 4 years. Consider a 1kW rooftop solar system.");
        }

        return tips;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private record DateRange(String label, LocalDateTime start, LocalDateTime end) {}

    private DateRange resolveDateRange(String period) {
        LocalDate today = LocalDate.now();
        if (period == null) period = "TODAY";
        return switch (period.toUpperCase()) {
            case "LAST_7_DAYS" -> new DateRange("Last 7 Days",
                    today.minusDays(6).atStartOfDay(), today.atTime(LocalTime.MAX));
            case "LAST_30_DAYS" -> new DateRange("Last 30 Days",
                    today.minusDays(29).atStartOfDay(), today.atTime(LocalTime.MAX));
            default -> new DateRange("Today",
                    today.atStartOfDay(), today.atTime(LocalTime.MAX));
        };
    }

    private String formatHour(int hour) {
        if (hour == 0) return "12 AM";
        if (hour == 12) return "12 PM";
        if (hour < 12) return hour + " AM";
        return (hour - 12) + " PM";
    }

    private double safe(Double v) { return v == null ? 0.0 : v; }
    private double round1(double v) { return Math.round(v * 10.0) / 10.0; }
    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    private double round3(double v) { return Math.round(v * 1000.0) / 1000.0; }
}
