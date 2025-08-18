package com.fourmen.meetingplatform.domain.template.dto.response;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fourmen.meetingplatform.domain.template.entity.Template;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TemplateResponse {
    private Long templateId;
    private String templateName;
    private String eformsignTemplateId;
    private String previewImageUrl;

    @JsonRawValue
    private String dataSchema;

    public static TemplateResponse from(Template template) {
        return TemplateResponse.builder()
                .templateId(template.getId())
                .templateName(template.getTemplateName())
                .eformsignTemplateId(template.getEformsignTemplateId())
                .previewImageUrl(template.getPreviewImageUrl())
                .dataSchema(template.getDataSchema())
                .build();
    }
}