package com.aicopilot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class MatchHistoryItem {
    private Long id;
    private Long resumeId;
    private String resumeTitle;
    private String jobDescription;
    private BigDecimal matchScore;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
    private LocalDateTime createdAt;
}
