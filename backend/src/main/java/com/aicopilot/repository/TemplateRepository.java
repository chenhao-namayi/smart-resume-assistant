package com.aicopilot.repository;

import com.aicopilot.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    List<Template> findByCategoryOrderByNameAsc(String category);
    List<Template> findAllByOrderByCategoryAscNameAsc();
    boolean existsBySourceUrl(String sourceUrl);
}
