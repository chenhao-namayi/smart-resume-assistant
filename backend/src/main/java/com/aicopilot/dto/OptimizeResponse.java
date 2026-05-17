package com.aicopilot.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class OptimizeResponse {
    private String optimizedText;
    private Integer tokensUsed;
}
