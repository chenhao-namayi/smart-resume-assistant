package com.aicopilot.service;

import com.aicopilot.dto.*;
import com.aicopilot.entity.InterviewSession;
import com.aicopilot.entity.Resume;
import com.aicopilot.entity.User;
import com.aicopilot.exception.BusinessException;
import com.aicopilot.repository.InterviewSessionRepository;
import com.aicopilot.repository.ResumeRepository;
import com.aicopilot.repository.UserRepository;
import com.aicopilot.util.PromptBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    private static final int MAX_QUESTIONS = 6;

    private final InterviewSessionRepository sessionRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final LLMService llmService;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Transactional
    public InterviewResponse startInterview(InterviewStartRequest request, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(request.getResumeId(), userId)
                .orElseThrow(() -> new BusinessException(404, "简历不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        String resumeJson = resume.getContentJson() != null ? resume.getContentJson() : "{}";
        String systemPrompt = promptBuilder.buildInterviewSystemPrompt();
        String userPrompt = promptBuilder.buildInterviewStartUserPrompt(resumeJson, request.getPosition());
        String question = llmService.callLLM(systemPrompt, userPrompt);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> firstMsg = new HashMap<>();
        firstMsg.put("role", "ai");
        firstMsg.put("content", question);
        firstMsg.put("timestamp", LocalDateTime.now().toString());
        messages.add(firstMsg);

        InterviewSession session = new InterviewSession();
        session.setUser(user);
        session.setResume(resume);
        session.setPosition(request.getPosition());
        session.setMessages(toJson(messages));
        session.setStatus("IN_PROGRESS");
        session = sessionRepository.save(session);

        return new InterviewResponse(session.getId(), question, "IN_PROGRESS", 1);
    }

    @Transactional
    public InterviewResponse submitAnswer(InterviewAnswerRequest request, Long userId) {
        InterviewSession session = sessionRepository.findByIdAndUserId(request.getSessionId(), userId)
                .orElseThrow(() -> new BusinessException(404, "面试会话不存在"));
        if (!"IN_PROGRESS".equals(session.getStatus())) {
            throw new BusinessException(400, "面试已结束");
        }

        List<Map<String, String>> messages = parseMessages(session.getMessages());
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", request.getAnswer());
        userMsg.put("timestamp", LocalDateTime.now().toString());
        messages.add(userMsg);

        int questionCount = countAiMessages(messages);
        if (questionCount >= MAX_QUESTIONS) {
            return autoEndInterview(session, messages, userId);
        }

        String history = buildConversationHistory(messages);
        String systemPrompt = promptBuilder.buildInterviewSystemPrompt();
        String userPrompt = promptBuilder.buildInterviewNextUserPrompt(history, request.getAnswer());
        String response = llmService.callLLM(systemPrompt, userPrompt);

        boolean shouldEnd = response.contains("[END]");
        String question = response.replace("[END]", "").trim();

        Map<String, String> aiMsg = new HashMap<>();
        aiMsg.put("role", "ai");
        aiMsg.put("content", question);
        aiMsg.put("timestamp", LocalDateTime.now().toString());
        messages.add(aiMsg);

        session.setMessages(toJson(messages));
        if (shouldEnd) {
            session.setStatus("COMPLETED");
            session.setCompletedAt(LocalDateTime.now());
        }
        session = sessionRepository.save(session);

        return new InterviewResponse(session.getId(), question,
                shouldEnd ? "COMPLETED" : "IN_PROGRESS", countAiMessages(messages));
    }

    @Transactional
    public InterviewReportResponse endInterview(Long sessionId, Long userId) {
        InterviewSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(404, "面试会话不存在"));

        List<Map<String, String>> messages = parseMessages(session.getMessages());
        return generateReport(session, messages);
    }

    public InterviewSession getSessionDetail(Long sessionId, Long userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(404, "面试会话不存在"));
    }

    public List<InterviewSessionSummary> getUserHistory(Long userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(InterviewSessionSummary::from)
                .toList();
    }

    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        InterviewSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(404, "面试会话不存在"));
        sessionRepository.delete(session);
    }

    public Page<InterviewSession> listAllInterviews(int page, int size) {
        return sessionRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    // ===== Private helpers =====

    private InterviewResponse autoEndInterview(InterviewSession session, List<Map<String, String>> messages, Long userId) {
        InterviewReportResponse report = generateReport(session, messages);
        return new InterviewResponse(session.getId(),
                "面试已结束，报告已生成。评分：" + report.getScore() + "分",
                "COMPLETED", countAiMessages(messages));
    }

    private InterviewReportResponse generateReport(InterviewSession session, List<Map<String, String>> messages) {
        String history = buildConversationHistory(messages);
        String resumeJson = session.getResume().getContentJson() != null ? session.getResume().getContentJson() : "{}";
        String systemPrompt = promptBuilder.buildInterviewReportSystemPrompt();
        String userPrompt = promptBuilder.buildInterviewReportUserPrompt(resumeJson, history);
        String llmResponse = llmService.callLLM(systemPrompt, userPrompt);

        try {
            String json = extractJson(llmResponse);
            @SuppressWarnings("unchecked")
            Map<String, Object> report = objectMapper.readValue(json, Map.class);

            int score = report.get("score") instanceof Integer ? (Integer) report.get("score") : 0;
            session.setScore(score);
            session.setReport((String) report.get("report"));
            session.setStrengths(toJson(report.get("strengths")));
            session.setWeaknesses(toJson(report.get("weaknesses")));
            session.setSuggestions(toJson(report.get("suggestions")));
        } catch (Exception e) {
            log.error("Failed to parse interview report: {}", e.getMessage());
            session.setScore(0);
            session.setReport(llmResponse);
            session.setStrengths("[]");
            session.setWeaknesses("[]");
            session.setSuggestions("[]");
        }

        if (!"COMPLETED".equals(session.getStatus())) {
            session.setStatus("COMPLETED");
            session.setCompletedAt(LocalDateTime.now());
        }
        session.setMessages(toJson(messages));
        session = sessionRepository.save(session);
        return InterviewReportResponse.from(session);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseMessages(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private int countAiMessages(List<Map<String, String>> messages) {
        return (int) messages.stream().filter(m -> "ai".equals(m.get("role"))).count();
    }

    private String buildConversationHistory(List<Map<String, String>> messages) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
            Map<String, String> msg = messages.get(i);
            String label = "ai".equals(msg.get("role")) ? "面试官" : "候选人";
            sb.append(label).append(": ").append(msg.get("content")).append("\n\n");
        }
        return sb.toString();
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}
