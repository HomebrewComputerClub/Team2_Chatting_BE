package com.chatting.homebrewchat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ChatDto {
    @Data
    @Builder
    public static class makeRoomReq{
        private Long targetMemberId;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class roomListRes{
        private String roomId;
        private String targetName;
        private Long targetMemberId;
        private String targetImage;
        private String lastContent;
        private LocalDateTime lastSendTime;
    }
    @Data
    @Builder
    public static class messageListInfo{
        private String roomId;
        List<messageInfo> messageList;
    }
    @Data
    @Builder
    public static class messageInfo{
        private String messageId;
        //보내는 사람
        private String senderName;
        private String detail;
    }
}
