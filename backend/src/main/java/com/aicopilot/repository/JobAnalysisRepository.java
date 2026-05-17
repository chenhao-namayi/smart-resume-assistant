package com.aicopilot.repository;

import com.aicopilot.entity.JobAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface JobAnalysisRepository extends JpaRepository<JobAnalysis, Long> {
    List<JobAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<JobAnalysis> findByResumeId(Long resumeId);
    Page<JobAnalysis> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long count();
}
