package com.fourmen.meetingplatform.domain.chat.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatRoomType {
    COMPANY("회사 채팅"),
    PERSONAL("개인 채팅");

    private final String description;
}