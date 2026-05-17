package com.aicopilot.dto;

import com.aicopilot.entity.InterviewSession;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewSessionSummary {
    private Long id;
    private Long resumeId;
    private String resumeTitle;
    private String position;
    private String status;
    private Integer score;
    private Integer questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private static final ObjectMapper mapper = new ObjectMapper();

    public static InterviewSessionSummary from(InterviewSession session) {
        InterviewSessionSummary s = new InterviewSessionSummary();
        s.setId(session.getId());
        s.setResumeId(session.getResume().getId());
        s.setResumeTitle(session.getResume().getTitle());
        s.setPosition(session.getPosition());
        s.setStatus(session.getStatus());
        s.setScore(session.getScore());
        s.setCreatedAt(session.getCreatedAt());
        s.setCompletedAt(session.getCompletedAt());
        try {
            if (session.getMessages() != null) {
                var msgs = mapper.readValue(session.getMessages(), new TypeReference<java.util.List<Object>>() {});
                s.setQuestionCount((int) msgs.stream().filter(m -> m instanceof java.util.Map && "ai".equals(((java.util.Map<?, ?>) m).get("role"))).count());
            }
        } catch (Exception e) {
            s.setQuestionCount(0);
        }
        return s;
    }
}
