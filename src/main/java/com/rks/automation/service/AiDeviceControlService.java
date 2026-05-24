package com.rks.automation.service;

import com.rks.automation.dto.ai.AiDeviceCommandResponse;

public interface AiDeviceControlService {

    AiDeviceCommandResponse executeCommand(String username, String command);
}
