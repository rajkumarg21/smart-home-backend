package com.rks.automation.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiDeviceCommandRequest {

    @NotBlank(message = "Command is required")
    private String command;
}
