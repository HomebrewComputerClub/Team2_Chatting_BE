package com.chatting.homebrewchat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import java.nio.file.AccessDeniedException;
@Slf4j
public class CustomStompInterceptor implements ChannelInterceptor {
    //token provider..

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        log.info("Got Msg,,"+message);
//        if(accessor.getCommand() == StompCommand.CONNECT) {
//            if(!tokenProvider.validateToken(accessor.getFirstNativeHeader("Authorization")))
//                throw new AccessDeniedException("");
//        }
        return message;
    }
}
