package com.rks.automation.service;

import com.rks.automation.dto.ai.AiCommandDecision;
import com.rks.automation.dto.device.DeviceResponse;

import java.util.List;
import java.util.Optional;

public interface SmartHomeAiAgent {

    Optional<AiCommandDecision> interpret(String command, List<DeviceResponse> devices);
}
