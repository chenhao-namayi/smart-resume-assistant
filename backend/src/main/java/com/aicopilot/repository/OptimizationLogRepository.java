package com.aicopilot.repository;

import com.aicopilot.entity.OptimizationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface OptimizationLogRepository extends JpaRepository<OptimizationLog, Long> {
    List<OptimizationLog> findByResumeIdOrderByCreatedAtDesc(Long resumeId);
    Page<OptimizationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long count();
}
