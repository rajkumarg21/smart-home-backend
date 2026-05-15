package com.rks.automation.dto.device;

import java.time.LocalDateTime;


import com.rks.automation.entity.Device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Read-only DTO returned to clients for device data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {

    private Long id;
    private String name;
    private String type;
    private String location;
    private String status;
    private String metadata;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Convenience factory from entity. */
    public static DeviceResponse from(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .name(device.getName())
                .type(device.getType())
                .location(device.getLocation())
                .status(device.getStatus())
                .metadata(device.getMetadata())
                .userId(device.getUser().getId())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
}
