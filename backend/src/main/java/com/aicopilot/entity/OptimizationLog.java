package com.aicopilot.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "optimization_logs")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OptimizationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "prompt_used", columnDefinition = "TEXT")
    private String promptUsed;

    @Column(name = "llm_model", length = 50)
    private String llmModel;

    @Column(name = "input_text", columnDefinition = "LONGTEXT")
    private String inputText;

    @Column(name = "output_text", columnDefinition = "LONGTEXT")
    private String outputText;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "section_type", length = 50)
    private String sectionType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
