package com.rks.automation.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.rks.automation.entity.DeviceAnalytics;

import java.time.LocalDateTime;
import java.util.List;

public interface DeviceAnalyticsRepository extends JpaRepository<DeviceAnalytics, Long> {
    List<DeviceAnalytics> findByDeviceIdOrderByRecordedAtAsc(Long deviceId);

    List<DeviceAnalytics> findByDeviceUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long userId, LocalDateTime start, LocalDateTime end);

    List<DeviceAnalytics> findByDeviceIdAndDeviceUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long deviceId, Long userId, LocalDateTime start, LocalDateTime end);
}
