package com.rks.automation.service;

import java.util.List;

import com.rks.automation.dto.deviceAnalytics.DeviceAnalyticsResponse;

public interface AnalyticsService {
	List<DeviceAnalyticsResponse> getAnalyticsForDevice(Long deviceId);
}
