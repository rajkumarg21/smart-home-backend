package com.rks.automation.dto.device;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload for POST /device/add
 */
@Data
public class DeviceRequest {

    @NotBlank(message = "Device name is required")
    private String name;

    @NotBlank(message = "Device type is required")
    private String type;

    private String location;

    private String metadata;
}