package com.fourmen.meetingplatform.domain.contract.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ContractSendRequestDto {
    private Document document;

    @Getter
    @NoArgsConstructor
    public static class Document {
        @JsonProperty("document_name")
        private String documentName;
        private String comment;
        private List<Recipient> recipients;
        private List<Field> fields;
        @JsonProperty("select_group_name")
        private String selectGroupName;
        private List<NotificationRecipient> notification;
    }

    @Getter
    @NoArgsConstructor
    public static class Recipient {
        @JsonProperty("step_type")
        private String stepType;
        @JsonProperty("use_mail")
        private boolean useMail;
        @JsonProperty("use_sms")
        private boolean useSms;
        private Member member;
        private Auth auth;
    }

    @Getter
    @NoArgsConstructor
    public static class Member {
        private String name;
        private String id;
        private Sms sms;
    }

    @Getter
    @NoArgsConstructor
    public static class Sms {
        @JsonProperty("country_code")
        private String countryCode;
        @JsonProperty("phone_number")
        private String phoneNumber;
    }

    @Getter
    @NoArgsConstructor
    public static class Auth {
        private String password;
        @JsonProperty("password_hint")
        private String passwordHint;
        private Valid valid;
    }

    @Getter
    @NoArgsConstructor
    public static class Valid {
        private int day;
        private int hour;
    }

    @Getter
    @NoArgsConstructor
    public static class Field {
        private String id;
        private String value;
    }

    @Getter
    @NoArgsConstructor
    public static class NotificationRecipient {
        private String name;
        private String email;
        private Sms sms;
        private NotificationAuth auth;
    }

    @Getter
    @NoArgsConstructor
    public static class NotificationAuth extends Auth {
        @JsonProperty("mobile_verification")
        private boolean mobileVerification;
    }
}