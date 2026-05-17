package com.aicopilot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatsResponse {
    private long totalUsers;
    private long totalResumes;
    private long totalTemplates;
    private long totalOptimizationLogs;
    private long totalJobAnalyses;
    private long totalInterviewSessions;
}
