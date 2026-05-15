package com.rks.automation.controller;

import com.rks.automation.common.ApiResponse;
import com.rks.automation.dto.device.DeviceRequest;
import com.rks.automation.dto.device.DeviceResponse;
import com.rks.automation.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Device management endpoints — all require authentication.
 *
 * GET    /device/list              → list all devices of the logged-in user
 * POST   /device/add               → add a new device
 * PUT    /device/control           → turn a device ON or OFF
 * DELETE /device/delete/{deviceId} → delete a device
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * GET /device/list
     * Returns all devices belonging to the authenticated user.
     */
    @GetMapping("/device/list")
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> getDevices(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<DeviceResponse> devices = deviceService.getDevices(userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Devices fetched successfully", devices));
    }

    /**
     * POST /device/add
     * Body: { "name", "type", "location", "metadata" }
     */
    @PostMapping("/device/add")
    public ResponseEntity<ApiResponse<DeviceResponse>> addDevice(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DeviceRequest request) {

        DeviceResponse device = deviceService.addDevice(userDetails.getUsername(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Device added successfully", device));
    }

    /**
     * PUT /device/control
     * Body: { "deviceId": 1, "action": "ON" | "OFF" }
     */
    @PutMapping("/device/control")
    public ResponseEntity<ApiResponse<DeviceResponse>> controlDevice(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {

        Long deviceId = Long.valueOf(body.get("deviceId").toString());
        String action  = body.get("action").toString();

        DeviceResponse device = deviceService.controlDevice(
                userDetails.getUsername(), deviceId, action);

        return ResponseEntity.ok(
                ApiResponse.success("Device " + action.toUpperCase() + " successfully", device));
    }

    /**
     * DELETE /device/delete/{deviceId}
     */
    @DeleteMapping("/device/delete/{deviceId}")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long deviceId) {

        deviceService.deleteDevice(userDetails.getUsername(), deviceId);
        return ResponseEntity.ok(
                ApiResponse.success("Device deleted successfully", null));
    }
}
