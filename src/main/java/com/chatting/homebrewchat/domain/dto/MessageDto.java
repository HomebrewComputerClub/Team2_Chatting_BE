package com.chatting.homebrewchat.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageDto {

    private MessageType type;
    //채팅방 ID
    private String roomId;
    //보내는 사람
    private String sender;
    private String detail;
}
