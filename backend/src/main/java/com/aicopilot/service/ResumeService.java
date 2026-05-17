package com.aicopilot.service;

import com.aicopilot.dto.ResumeCreateRequest;
import com.aicopilot.dto.ResumeSummary;
import com.aicopilot.dto.ResumeUpdateRequest;
import com.aicopilot.entity.Resume;
import com.aicopilot.entity.User;
import com.aicopilot.exception.BusinessException;
import com.aicopilot.repository.JobAnalysisRepository;
import com.aicopilot.repository.OptimizationLogRepository;
import com.aicopilot.repository.ResumeRepository;
import com.aicopilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final OptimizationLogRepository optimizationLogRepository;

    public List<ResumeSummary> getUserResumes(Long userId) {
        return resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(ResumeSummary::from)
                .collect(Collectors.toList());
    }

    public Resume getResumeById(Long resumeId, Long userId) {
        return resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new BusinessException(404, "简历不存在"));
    }

    @Transactional
    public Resume createResume(ResumeCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setTitle(request.getTitle());
        resume.setContentJson(request.getContentJson());
        resume.setVersion(1);
        resume.setIsCurrent(true);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume updateResume(Long resumeId, ResumeUpdateRequest request, Long userId) {
        Resume resume = getResumeById(resumeId, userId);
        if (request.getTitle() != null) {
            resume.setTitle(request.getTitle());
        }
        if (request.getContentJson() != null) {
            resume.setContentJson(request.getContentJson());
        }
        if (request.getIsCurrent() != null) {
            resume.setIsCurrent(request.getIsCurrent());
        }
        return resumeRepository.save(resume);
    }

    @Transactional
    public void deleteResume(Long resumeId, Long userId) {
        Resume resume = getResumeById(resumeId, userId);
        jobAnalysisRepository.deleteAll(jobAnalysisRepository.findByResumeId(resumeId));
        optimizationLogRepository.deleteAll(optimizationLogRepository.findByResumeIdOrderByCreatedAtDesc(resumeId));
        resumeRepository.delete(resume);
    }

    @Transactional
    public Resume createVersion(Long resumeId, Long userId) {
        Resume current = getResumeById(resumeId, userId);

        Resume newVersion = new Resume();
        newVersion.setUser(current.getUser());
        newVersion.setTitle(current.getTitle());
        newVersion.setContentJson(current.getContentJson());
        newVersion.setVersion(current.getVersion() + 1);
        newVersion.setOptimizedFrom(current);
        newVersion.setIsCurrent(true);

        current.setIsCurrent(false);
        resumeRepository.save(current);

        return resumeRepository.save(newVersion);
    }
}
