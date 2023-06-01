package com.chatting.homebrewchat.controller;

import com.chatting.homebrewchat.domain.dto.ChatDto;
import com.chatting.homebrewchat.domain.dto.DirectMessageDto;
import com.chatting.homebrewchat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final SimpMessageSendingOperations sendingOperations;
    private final ChatService chatService;
    @MessageMapping("/message/send/direct")
    public void sendMessage(DirectMessageDto message){
        log.info("Got Message:"+message);
        chatService.saveChatMessage(message);
//        message.setDetail(message.getDetail()+", 서버에서 확인");
        sendingOperations.convertAndSend("/direct/room/"+message.getRoomId(),message);
    }

    @PostMapping("/api/createRoom/{id}")
    @Operation(summary = "1대1 채팅방 생성(임시)", description = "(원래) 1대1 대화를 하고싶은 상대방의 memberId로 해당 유저와의 채팅방 id를 얻는다.")
    public ResponseEntity<String> makeDirectRoom(@PathVariable Long id){
        return new ResponseEntity<>(chatService.getDirectRoomId(id), HttpStatus.OK);
    }
    @GetMapping("/api/getRoomList/direct")
    @Operation(summary = "자신의 1대1 채팅방 목록 조회", description = "1대1 채팅방 목록을 조히(인증 도입 후엔 memberId를 받지 않는다.)")
    public ResponseEntity<List<ChatDto.roomListRes>> getMyRoom(){
        return new ResponseEntity<>(chatService.getMyRoomList(),HttpStatus.OK);
    }
    @GetMapping("/api/getChatList/{roomId}")
    @Operation(summary = "이전 채팅 기록 조회", description = "1대1 채팅방의 이전 채팅 기록을 조회하는 API")
    public ResponseEntity<ChatDto.messageListInfo> getChatList(@PathVariable String roomId){
        return new ResponseEntity<>(chatService.getDirectMessageList(roomId),HttpStatus.OK);
    }
}
