package com.rks.automation.service;

import com.rks.automation.dto.device.DeviceRequest;
import com.rks.automation.dto.device.DeviceResponse;

import java.util.List;

/**
 * Contract for device management operations.
 */
public interface DeviceService {

    /** List all devices owned by the authenticated user. */
    List<DeviceResponse> getDevices(String username);

    /** Add a new device for the authenticated user. */
    DeviceResponse addDevice(String username, DeviceRequest request);

    /** Update a device owned by the authenticated user. */
    DeviceResponse updateDevice(String username, Long deviceId, DeviceRequest request);

    /** Control a device — action is "ON" or "OFF". */
    DeviceResponse controlDevice(String username, Long deviceId, String action);

    /** Control a device and record the command source for device communication. */
    DeviceResponse controlDevice(String username, Long deviceId, String action, String source, String command);

    /** Delete a device owned by the authenticated user. */
    void deleteDevice(String username, Long deviceId);
}
