package com.chatting.homebrewchat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ChatDto {
    @Data
    @Builder
    public static class makeRoomReq{
        private Long myMemberId;
        private Long targetMemberId;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class roomListRes{
        private List<String> roomId;
    }
}
