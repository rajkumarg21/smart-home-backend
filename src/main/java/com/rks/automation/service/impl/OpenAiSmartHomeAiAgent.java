package com.rks.automation.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rks.automation.dto.ai.AiCommandDecision;
import com.rks.automation.dto.device.DeviceResponse;
import com.rks.automation.service.SmartHomeAiAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.AbstractMap.SimpleEntry;

@Slf4j
@Service
public class OpenAiSmartHomeAiAgent implements SmartHomeAiAgent {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ai.openai.enabled:false}")
    private boolean enabled;

    @Value("${app.ai.openai.api-key:}")
    private String apiKey;

    @Value("${app.ai.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${app.ai.openai.base-url:https://api.openai.com/v1/responses}")
    private String baseUrl;

    @Override
    public Optional<AiCommandDecision> interpret(String command, List<DeviceResponse> devices) {
        if (!enabled || apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        try {
            String prompt = buildPrompt(command, devices);
            Map<String, Object> request = Map.of(
                    "model", model,
                    "temperature", 0,
                    "instructions", """
                            You are SmartHomeCommandAgent, a home automation AI agent.
                            Convert the user command into exactly one JSON object.
                            Never execute actions yourself. The backend will execute safe tools.
                            Allowed intents: CONTROL, RUNTIME_ANALYTICS, ENERGY_ANALYTICS, CONFIG_QUERY, DEVICE_COUNT, UNKNOWN.
                            Allowed actions: ON, OFF, or null.
                            Allowed device types: LIGHT, FAN, AC, LOCK, CAMERA, OTHER, or null.
                            Allowed periods: TODAY, LAST_1_WEEK, LAST_1_MONTH, CUSTOM_DATE, or null.
                            If the request controls all devices except one type, set targetAll=true and excludeType.
                            If the request controls only lights/fans/etc, set targetType.
                            For a custom date, set period=CUSTOM_DATE and date as ISO yyyy-MM-dd.
                            For location questions, set location such as hall, kitchen, living room, porch, gallery, bedroom, washroom.
                            Return only JSON with keys: intent, action, targetAll, targetType, excludeType, deviceName, location, period, date, responseHint.
                            """,
                    "input", prompt,
                    "text", Map.of("format", Map.of("type", "json_object"))
            );

            String response = RestClient.create()
                    .post()
                    .uri(baseUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);

            String jsonText = extractOutputText(response);
            return Optional.of(objectMapper.readValue(jsonText, AiCommandDecision.class));
        } catch (Exception ex) {
            log.warn("AI agent interpretation failed. Falling back to local rules.", ex);
            return Optional.empty();
        }
    }

    private String buildPrompt(String command, List<DeviceResponse> devices) throws Exception {
        List<Map<String, Object>> catalog = devices.stream()
                .map(device -> Map.<String, Object>ofEntries(
                        new SimpleEntry<>("id", device.getId()),
                        new SimpleEntry<>("name", device.getName()),
                        new SimpleEntry<>("type", device.getType()),
                        new SimpleEntry<>("location", device.getLocation() == null ? "" : device.getLocation()),
                        new SimpleEntry<>("status", device.getStatus()),
                        new SimpleEntry<>("company", device.getCompany() == null ? "" : device.getCompany()),
                        new SimpleEntry<>("watt", device.getWatt() == null ? "" : device.getWatt()),
                        new SimpleEntry<>("brightness", device.getBrightness() == null ? "" : device.getBrightness()),
                        new SimpleEntry<>("speed", device.getSpeed() == null ? "" : device.getSpeed()),
                        new SimpleEntry<>("temperature", device.getTemperature() == null ? "" : device.getTemperature())))
                .toList();

        return objectMapper.writeValueAsString(Map.of(
                "userCommand", command,
                "availableDevices", catalog,
                "examples", List.of(
                        "turn off all lights -> CONTROL OFF targetType LIGHT",
                        "turn on all devices except fan -> CONTROL ON targetAll true excludeType FAN",
                        "fan running time today -> RUNTIME_ANALYTICS targetType FAN period TODAY",
                        "energy consume last 1 month -> ENERGY_ANALYTICS targetAll true period LAST_1_MONTH",
                        "running time of bedroom fan on 2026-05-20 -> RUNTIME_ANALYTICS deviceName Bedroom Fan period CUSTOM_DATE date 2026-05-20",
                        "configuration of hall light -> CONFIG_QUERY targetType LIGHT location hall",
                        "total devices on in kitchen -> DEVICE_COUNT location kitchen"
                )));
    }

    private String extractOutputText(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        JsonNode outputText = root.path("output_text");
        if (outputText.isTextual()) {
            return outputText.asText();
        }

        JsonNode output = root.path("output");
        if (output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.path("content");
                if (content.isArray()) {
                    for (JsonNode contentItem : content) {
                        JsonNode text = contentItem.path("text");
                        if (text.isTextual()) {
                            return text.asText();
                        }
                    }
                }
            }
        }
        throw new IllegalArgumentException("OpenAI response did not contain output text.");
    }
}
