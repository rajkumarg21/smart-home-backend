package com.rks.automation.service.impl;

import com.rks.automation.dto.device.DeviceCommandMessage;
import com.rks.automation.dto.device.DeviceResponse;
import com.rks.automation.service.DeviceCommandPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketDeviceCommandPublisher implements DeviceCommandPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publish(DeviceResponse device, String action, String source, String command) {
        DeviceCommandMessage message = DeviceCommandMessage.from(device, action, source, command);
        messagingTemplate.convertAndSend("/topic/device", device);
        messagingTemplate.convertAndSend("/topic/device/" + device.getId() + "/command", message);
    }
}
