package com.rks.automation.dto.device;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCommandMessage {

    private Long deviceId;
    private String deviceName;
    private String action;
    private String source;
    private String command;
    private LocalDateTime sentAt;

    public static DeviceCommandMessage from(DeviceResponse device, String action, String source, String command) {
        return DeviceCommandMessage.builder()
                .deviceId(device.getId())
                .deviceName(device.getName())
                .action(action)
                .source(source)
                .command(command)
                .sentAt(LocalDateTime.now())
                .build();
    }
}
