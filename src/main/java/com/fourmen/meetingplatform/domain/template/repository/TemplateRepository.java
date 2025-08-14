package com.fourmen.meetingplatform.domain.template.repository;

import com.fourmen.meetingplatform.domain.template.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    List<Template> findByCompanyId(Long companyId);
}