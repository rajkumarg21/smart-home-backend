package com.rks.automation.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiUsageNotification {

    private String type;
    private String title;
    private String message;
    private AiUsageReportResponse report;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
