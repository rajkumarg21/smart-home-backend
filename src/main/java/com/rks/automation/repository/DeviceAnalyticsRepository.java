package com.rks.automation.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.rks.automation.entity.DeviceAnalytics;

import java.util.List;

public interface DeviceAnalyticsRepository extends JpaRepository<DeviceAnalytics, Long> {
    List<DeviceAnalytics> findByDeviceIdOrderByRecordedAtAsc(Long deviceId);
}
