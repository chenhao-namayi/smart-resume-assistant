package com.aicopilot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterviewResponse {
    private Long sessionId;
    private String question;
    private String status;
    private Integer questionNumber;
}
