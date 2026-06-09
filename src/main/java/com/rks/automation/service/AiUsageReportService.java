package com.rks.automation.service;

import com.rks.automation.dto.ai.AiUsageReportResponse;

public interface AiUsageReportService {

    AiUsageReportResponse generateReport(String username, String period, boolean notifyUser);
}
