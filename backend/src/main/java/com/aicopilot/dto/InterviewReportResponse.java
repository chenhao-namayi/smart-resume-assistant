package com.aicopilot.dto;

import com.aicopilot.entity.InterviewSession;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewReportResponse {
    private Long sessionId;
    private Long resumeId;
    private String resumeTitle;
    private Integer totalQuestions;
    private Integer score;
    private String report;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private static final ObjectMapper mapper = new ObjectMapper();

    public static InterviewReportResponse from(InterviewSession session) {
        InterviewReportResponse resp = new InterviewReportResponse();
        resp.setSessionId(session.getId());
        resp.setResumeId(session.getResume().getId());
        resp.setResumeTitle(session.getResume().getTitle());
        resp.setScore(session.getScore());
        resp.setReport(session.getReport());
        resp.setStrengths(parseJsonArray(session.getStrengths()));
        resp.setWeaknesses(parseJsonArray(session.getWeaknesses()));
        resp.setSuggestions(parseJsonArray(session.getSuggestions()));
        resp.setCreatedAt(session.getCreatedAt());
        resp.setCompletedAt(session.getCompletedAt());
        try {
            List<Object> msgs = mapper.readValue(session.getMessages(), new TypeReference<>() {});
            resp.setTotalQuestions((int) msgs.stream().filter(m -> m instanceof java.util.Map && "ai".equals(((java.util.Map<?, ?>) m).get("role"))).count());
        } catch (Exception e) {
            resp.setTotalQuestions(0);
        }
        return resp;
    }

    private static List<String> parseJsonArray(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.singletonList(json);
        }
    }
}
