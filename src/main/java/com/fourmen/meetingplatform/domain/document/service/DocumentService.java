package com.fourmen.meetingplatform.domain.document.service;

import com.fourmen.meetingplatform.domain.document.dto.response.ContractInfoDto;
import com.fourmen.meetingplatform.domain.document.dto.response.DocumentResponseDto;
import com.fourmen.meetingplatform.domain.document.dto.response.MeetingInfoDto;
import com.fourmen.meetingplatform.domain.document.dto.response.MeetingsWithDocsDto;
import com.fourmen.meetingplatform.domain.document.dto.response.MinuteInfoDto;
import com.fourmen.meetingplatform.domain.document.dto.response.StandaloneContractDto;
import com.fourmen.meetingplatform.domain.document.repository.DocumentRepository;
import com.fourmen.meetingplatform.domain.minutes.entity.MinutesType;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public DocumentResponseDto getDocuments(User user, String startDateStr, String endDateStr) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (startDateStr != null && endDateStr != null) {
            startDate = LocalDate.parse(startDateStr).atStartOfDay();
            endDate = LocalDate.parse(endDateStr).plusDays(1).atStartOfDay();
        } else if (startDateStr != null) {
            startDate = LocalDate.parse(startDateStr).atStartOfDay();
            endDate = LocalDate.parse(startDateStr).plusDays(1).atStartOfDay();
        } else if (endDateStr == null) {
            endDate = LocalDateTime.now().plusDays(1);
            startDate = endDate.minusWeeks(1);
        }

        List<Object[]> meetingResults = documentRepository.findMeetingsWithDocs(user.getId(), startDate, endDate);
        Map<LocalDate, Map<Long, MeetingInfoDto>> meetingsByDate = new LinkedHashMap<>();

        for (Object[] row : meetingResults) {
            Long meetingId = ((Number) row[0]).longValue();
            String meetingTitle = (String) row[1];
            LocalDate meetingDate = ((java.sql.Date) row[2]).toLocalDate();
            Long minuteId = row[3] != null ? ((Number) row[3]).longValue() : null;
            String minuteTypeStr = (String) row[4];
            MinutesType minuteType = minuteTypeStr != null ? MinutesType.valueOf(minuteTypeStr) : null;
            Long contractId = row[5] != null ? ((Number) row[5]).longValue() : null;
            String contractTitle = (String) row[6];

            MeetingInfoDto meetingInfo = meetingsByDate
                    .computeIfAbsent(meetingDate, k -> new LinkedHashMap<>())
                    .computeIfAbsent(meetingId, k -> MeetingInfoDto.builder()
                            .meetingId(meetingId)
                            .meetingTitle(meetingTitle)
                            .minutes(new ArrayList<>())
                            .build());

            if (minuteId != null) {
                MinuteInfoDto minuteInfo = meetingInfo.getMinutes().stream()
                        .filter(m -> m.getMinuteId().equals(minuteId))
                        .findFirst()
                        .orElseGet(() -> {
                            MinuteInfoDto newMinute = MinuteInfoDto.builder()
                                    .minuteId(minuteId)
                                    .type(minuteType)
                                    .contracts(new ArrayList<>())
                                    .build();
                            meetingInfo.getMinutes().add(newMinute);
                            return newMinute;
                        });

                if (contractId != null) {
                    minuteInfo.getContracts().add(ContractInfoDto.builder()
                            .contractId(contractId)
                            .title(contractTitle)
                            .build());
                }
            }
        }

        List<MeetingsWithDocsDto> meetingsWithDocs = meetingsByDate.entrySet().stream()
                .map(entry -> MeetingsWithDocsDto.builder()
                        .date(entry.getKey())
                        .meetings(new ArrayList<>(entry.getValue().values()))
                        .build())
                .collect(Collectors.toList());

        List<Object[]> standaloneResults = documentRepository.findStandaloneContracts(user.getId(), startDate, endDate);
        List<StandaloneContractDto> standaloneContracts = standaloneResults.stream()
                .map(row -> StandaloneContractDto.builder()
                        .contractId(((Number) row[0]).longValue())
                        .title((String) row[1])
                        .createdAt(((Timestamp) row[2]).toLocalDateTime())
                        .build())
                .collect(Collectors.toList());

        return DocumentResponseDto.builder()
                .meetingsWithDocs(meetingsWithDocs)
                .standaloneContracts(standaloneContracts)
                .build();
    }
}