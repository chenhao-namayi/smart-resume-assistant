package com.aicopilot.service;

import com.aicopilot.dto.*;
import com.aicopilot.entity.*;
import com.aicopilot.exception.BusinessException;
import com.aicopilot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final TemplateRepository templateRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final OptimizationLogRepository optimizationLogRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final PasswordEncoder passwordEncoder;

    // ===== User Management =====

    public List<UserManageResponse> listUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserManageResponse::from)
                .collect(Collectors.toList());
    }

    public UserManageResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return UserManageResponse.from(user);
    }

    @Transactional
    public UserManageResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (!request.getUsername().equals(user.getUsername())
                    && userRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException(400, "用户名已存在");
            }
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            try {
                user.setRole(Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException(400, "无效的角色类型");
            }
        }

        user = userRepository.save(user);
        return UserManageResponse.from(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException(400, "不能删除管理员账号");
        }

        List<Resume> resumes = resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        for (Resume resume : resumes) {
            jobAnalysisRepository.deleteAll(jobAnalysisRepository.findByResumeId(resume.getId()));
            optimizationLogRepository.deleteAll(
                    optimizationLogRepository.findByResumeIdOrderByCreatedAtDesc(resume.getId()));
        }
        resumeRepository.deleteAll(resumes);
        userRepository.delete(user);
    }

    // ===== Resume Management =====

    public List<Resume> getUserResumes(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(404, "用户不存在");
        }
        return resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Transactional
    public void deleteResume(Long userId, Long resumeId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(404, "用户不存在");
        }
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(404, "简历不存在"));

        jobAnalysisRepository.deleteAll(jobAnalysisRepository.findByResumeId(resumeId));
        optimizationLogRepository.deleteAll(
                optimizationLogRepository.findByResumeIdOrderByCreatedAtDesc(resumeId));
        resumeRepository.delete(resume);
    }

    // ===== Template CRUD =====

    public List<Template> listTemplates(String category) {
        if (category != null && !category.isEmpty()) {
            return templateRepository.findByCategoryOrderByNameAsc(category);
        }
        return templateRepository.findAllByOrderByCategoryAscNameAsc();
    }

    public Template getTemplate(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "模板不存在"));
    }

    @Transactional
    public Template createTemplate(TemplateCreateRequest request) {
        Template template = new Template();
        template.setName(request.getName());
        template.setCategory(request.getCategory());
        template.setDescription(request.getDescription());
        template.setContentJson(request.getContentJson());
        template.setSourceUrl(request.getSourceUrl());
        return templateRepository.save(template);
    }

    @Transactional
    public Template updateTemplate(Long id, TemplateUpdateRequest request) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "模板不存在"));

        if (request.getName() != null) {
            template.setName(request.getName());
        }
        if (request.getCategory() != null) {
            template.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        if (request.getContentJson() != null) {
            template.setContentJson(request.getContentJson());
        }
        if (request.getSourceUrl() != null) {
            template.setSourceUrl(request.getSourceUrl());
        }

        return templateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "模板不存在"));
        templateRepository.delete(template);
    }

    // ===== Statistics =====

    public StatsResponse getStats() {
        return new StatsResponse(
                userRepository.count(),
                resumeRepository.count(),
                templateRepository.count(),
                optimizationLogRepository.count(),
                jobAnalysisRepository.count(),
                interviewSessionRepository.count()
        );
    }


    public Page<OptimizationLog> listOptimizationLogs(int page, int size) {
        return optimizationLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    public Page<InterviewSession> listInterviews(int page, int size) {
        return interviewSessionRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    public Page<JobAnalysis> listJobAnalyses(int page, int size) {
        return jobAnalysisRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }
}
