package com.aicopilot.dto;

import lombok.Data;

@Data
public class TemplateUpdateRequest {
    private String name;
    private String category;
    private String description;
    private String contentJson;
    private String sourceUrl;
}
