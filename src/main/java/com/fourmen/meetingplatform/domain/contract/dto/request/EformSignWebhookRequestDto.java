package com.fourmen.meetingplatform.domain.contract.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EformSignWebhookRequestDto {

    @JsonProperty("webhook_id")
    private String webhookId;

    @JsonProperty("event_type")
    private String eventType;

    private DocumentEvent document;

    @JsonProperty("ready_document_pdf")
    private PdfReadyEvent readyDocumentPdf;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DocumentEvent {
        private String id;
        private String status;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PdfReadyEvent {
        @JsonProperty("document_id")
        private String documentId;

        @JsonProperty("export_ready_list")
        private List<String> exportReadyList;
    }

}