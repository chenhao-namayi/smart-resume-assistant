package com.aicopilot.dto;

import com.aicopilot.entity.Resume;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResumeSummary {
    private Long id;
    private String title;
    private Integer version;
    private Boolean isCurrent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ResumeSummary from(Resume resume) {
        ResumeSummary summary = new ResumeSummary();
        summary.setId(resume.getId());
        summary.setTitle(resume.getTitle());
        summary.setVersion(resume.getVersion());
        summary.setIsCurrent(resume.getIsCurrent());
        summary.setCreatedAt(resume.getCreatedAt());
        summary.setUpdatedAt(resume.getUpdatedAt());
        return summary;
    }
}
