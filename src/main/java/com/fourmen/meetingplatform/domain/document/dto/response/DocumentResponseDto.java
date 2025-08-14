package com.fourmen.meetingplatform.domain.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDto {
    private List<MeetingsWithDocsDto> meetingsWithDocs;
    private List<StandaloneContractDto> standaloneContracts;
}