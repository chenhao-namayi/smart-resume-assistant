package com.aicopilot.service;

import com.aicopilot.dto.MatchHistoryItem;
import com.aicopilot.dto.MatchResponse;
import com.aicopilot.entity.JobAnalysis;
import com.aicopilot.entity.Resume;
import com.aicopilot.entity.User;
import com.aicopilot.exception.BusinessException;
import com.aicopilot.repository.JobAnalysisRepository;
import com.aicopilot.repository.ResumeRepository;
import com.aicopilot.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobAnalysisService {

    private final JobAnalysisRepository jobAnalysisRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final LLMService llmService;
    private final ObjectMapper objectMapper;

    @Transactional
    public MatchResponse analyzeMatch(Long resumeId, String jobDescription, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new BusinessException(404, "简历不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        MatchResponse matchResult = llmService.analyzeMatch(
                resume.getContentJson() != null ? resume.getContentJson() : "",
                jobDescription
        );

        JobAnalysis analysis = new JobAnalysis();
        analysis.setUser(user);
        analysis.setResume(resume);
        analysis.setJobDescription(jobDescription);
        analysis.setMatchScore(BigDecimal.valueOf(matchResult.getScore()));
        try {
            analysis.setSuggestions(objectMapper.writeValueAsString(matchResult));
        } catch (JsonProcessingException e) {
            analysis.setSuggestions("{}");
        }
        jobAnalysisRepository.save(analysis);

        return matchResult;
    }

    public List<MatchHistoryItem> getAnalysisHistory(Long userId) {
        List<JobAnalysis> analyses = jobAnalysisRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return analyses.stream().map(a -> {
            List<String> strengths = List.of();
            List<String> weaknesses = List.of();
            List<String> suggestions = List.of();
            try {
                MatchResponse mr = objectMapper.readValue(a.getSuggestions(), MatchResponse.class);
                strengths = mr.getStrengths() != null ? mr.getStrengths() : List.of();
                weaknesses = mr.getWeaknesses() != null ? mr.getWeaknesses() : List.of();
                suggestions = mr.getSuggestions() != null ? mr.getSuggestions() : List.of();
            } catch (Exception ignored) {}
            return new MatchHistoryItem(
                    a.getId(),
                    a.getResume().getId(),
                    a.getResume().getTitle(),
                    a.getJobDescription(),
                    a.getMatchScore(),
                    strengths,
                    weaknesses,
                    suggestions,
                    a.getCreatedAt()
            );
        }).toList();
    }
}
