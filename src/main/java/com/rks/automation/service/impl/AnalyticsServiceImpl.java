package com.rks.automation.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.rks.automation.dto.deviceAnalytics.DeviceAnalyticsResponse;
import com.rks.automation.repository.DeviceAnalyticsRepository;
import com.rks.automation.service.AnalyticsService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl  implements AnalyticsService {
	
	 private final DeviceAnalyticsRepository analyticsRepository;

	
	    public List<DeviceAnalyticsResponse> getAnalyticsForDevice(Long deviceId) {
	        return analyticsRepository
	            .findByDeviceIdOrderByRecordedAtAsc(deviceId)
	            .stream()
	            .map(a -> DeviceAnalyticsResponse.builder()
	                .power(a.getPower())
	                .duration(a.getDuration())
	                .recordedAt(a.getRecordedAt())
	                .build())
	            .collect(Collectors.toList());
	    }
}
