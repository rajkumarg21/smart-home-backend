package com.rks.automation.service;

import com.rks.automation.dto.device.DeviceCommandMessage;
import com.rks.automation.dto.device.DeviceResponse;

public interface DeviceCommandPublisher {

    void publish(DeviceResponse device, String action, String source, String command);
}
