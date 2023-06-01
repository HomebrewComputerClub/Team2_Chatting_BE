package com.chatting.homebrewchat.config;

import com.chatting.homebrewchat.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class ChatConfig implements WebSocketMessageBrokerConfigurer {
//    private final CustomStompInterceptor customStompInterceptor;
    @Autowired
    private TokenProvider tokenProvider;
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/ws").setAllowedOrigins("http://localhost:3000","https://localhost:3000","https://localhost:3001",
                "https://cocobol.site"
                ,"http://172.30.1.3:3000","https://192.168.219.144:3000","https://192.168.219.174:3000","https://192.168.0.24:3000").setAllowedOriginPatterns("*");
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){
        //클라이언트가 /queue나 /topic으로 시작되는 경로로 들어온다면 구독 중 이다
        registry.enableSimpleBroker("/direct","/multi");
        //메세지를 구독자에게 배포하는 경우 /app 하단의 경로로 배달?
        registry.setApplicationDestinationPrefixes("/pub");
    }
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new CustomStompInterceptor(tokenProvider));
    }


}
