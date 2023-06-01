package com.chatting.homebrewchat.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectMessageDto {

    //채팅방 ID
    private String roomId;
    private String messageId;
    private MessageType type;
    //보내는 사람
    private String senderName;
    private String detail;
}
