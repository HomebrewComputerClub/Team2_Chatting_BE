package com.chatting.homebrewchat.controller;

import com.chatting.homebrewchat.domain.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/api")
@Slf4j
public class MessageController {
    private final SimpMessageSendingOperations sendingOperations;
    @MessageMapping("/api/message/send")
    @GetMapping
    public void sendMessage(MessageDto message){
        log.info("Got Message:"+message);
        message.setDetail("서버에서 확인했음다");
        sendingOperations.convertAndSend("/sub/chat/1",message);
    }
}
