package com.rks.automation.service.impl;

import com.rks.automation.dto.device.DeviceRequest;
import com.rks.automation.dto.device.DeviceResponse;
import com.rks.automation.entity.Device;
import com.rks.automation.entity.User;
import com.rks.automation.repository.DeviceRepository;
import com.rks.automation.repository.UserRepository;
import com.rks.automation.service.DeviceCommandPublisher;
import com.rks.automation.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of {@link DeviceService}.
 *
 * Every write operation uses findByIdAndUserId so a user can never
 * read, control, or delete a device they don't own.
 */
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository   userRepository;
    private final DeviceCommandPublisher deviceCommandPublisher;

    // ── GET /device/list ─────────────────────────────────────────────────────

    /**
     * Returns all devices that belong to the authenticated user.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevices(String username) {
        User user = resolveUser(username);
        return deviceRepository.findByUserId(user.getId())
                .stream()
                .map(DeviceResponse::from)
                .toList();
    }

    // ── POST /device/add ─────────────────────────────────────────────────────

    /**
     * Creates a new device and associates it with the authenticated user.
     * Default status is OFF.
     */
    @Override
    @Transactional
    public DeviceResponse addDevice(String username, DeviceRequest request) {
        User user = resolveUser(username);

        Device device = Device.builder()
                .name(request.getName())
                .type(request.getType())
                .location(request.getLocation())
                .metadata(request.getMetadata())
                .status("OFF")
                .user(user)
                .build();

        return DeviceResponse.from(deviceRepository.save(device));
    }

    // ── PUT /device/control ──────────────────────────────────────────────────

    /**
     * Turns a device ON or OFF.
     * Throws IllegalArgumentException if the device doesn't belong to the user
     * or if the action is not "ON" / "OFF".
     */
    @Override
    @Transactional
    public DeviceResponse controlDevice(String username, Long deviceId, String action) {
        return controlDevice(username, deviceId, action, "USER", action);
    }

    @Override
    @Transactional
    public DeviceResponse controlDevice(String username, Long deviceId, String action, String source, String command) {
        User user = resolveUser(username);

        Device device = deviceRepository.findByIdAndUserId(deviceId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Device not found with id: " + deviceId));

        String normalized = action.toUpperCase();
        if (!normalized.equals("ON") && !normalized.equals("OFF")) {
            throw new IllegalArgumentException(
                    "Invalid action '" + action + "'. Allowed values: ON, OFF");
        }

        device.setStatus(normalized);
        DeviceResponse response = DeviceResponse.from(deviceRepository.save(device));
        deviceCommandPublisher.publish(response, normalized, source, command);
        return response;
    }

    // ── DELETE /device/delete/{deviceId} ─────────────────────────────────────

    /**
     * Deletes a device owned by the authenticated user.
     * Throws IllegalArgumentException if the device doesn't belong to the user.
     */
    @Override
    @Transactional
    public void deleteDevice(String username, Long deviceId) {
        User user = resolveUser(username);

        Device device = deviceRepository.findByIdAndUserId(deviceId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Device not found with id: " + deviceId));

        deviceRepository.delete(device);
    }

    // ── Private helper ───────────────────────────────────────────────────────

    private User resolveUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));
    }
}
