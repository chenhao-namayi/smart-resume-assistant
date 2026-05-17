package com.aicopilot.dto;

import lombok.Data;

@Data
public class ResumeUpdateRequest {
    private String title;
    private String contentJson;
    private Boolean isCurrent;
}
