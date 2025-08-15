package com.fourmen.meetingplatform.infra.eformsign.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EformSignSendResponse {

    @JsonProperty("document")
    private DocumentInfo document;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DocumentInfo {
        @JsonProperty("id")
        private String id;

        @JsonProperty("document_name")
        private String documentName;
    }
}