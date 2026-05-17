package com.aicopilot.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {
    private Integer score;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
}
