package com.rks.automation.controller;

import com.rks.automation.common.ApiResponse;
import com.rks.automation.dto.ai.AiDeviceCommandRequest;
import com.rks.automation.dto.ai.AiDeviceCommandResponse;
import com.rks.automation.service.AiDeviceControlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiDeviceController {

    private final AiDeviceControlService aiDeviceControlService;

    @PostMapping("/device-command")
    public ResponseEntity<ApiResponse<AiDeviceCommandResponse>> executeDeviceCommand(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AiDeviceCommandRequest request) {

        AiDeviceCommandResponse response = aiDeviceControlService.executeCommand(
                userDetails.getUsername(), request.getCommand());

        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }
}
