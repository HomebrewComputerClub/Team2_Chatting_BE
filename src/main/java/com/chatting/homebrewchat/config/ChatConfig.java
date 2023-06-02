package com.chatting.homebrewchat.config;
import com.chatting.homebrewchat.config.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.chatting.homebrewchat.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class ChatConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(ChatConfig.class);

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        System.out.println("----->>> registerStompEndpoints <<<---");
        logger.info("----->>> registerStompEndpoints  구독<<<---");
        registry.addEndpoint("/api/ws")
                .setAllowedOrigins("http://localhost:3000", "https://localhost:3000", "https://localhost:3001",
                        "https://cocobol.site", "http://172.30.1.3:3000",
                        "https://192.168.219.144:3000", "https://192.168.219.174:3000", "https://192.168.0.24:3000",
                        "https://192.168.219.103:3000")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        logger.info("----->>> configureMessageBroker  발행<<<---");
        System.out.println("----->>> configureMessageBroker <<<---");
        registry.enableSimpleBroker("/direct", "/multi");
        registry.setApplicationDestinationPrefixes("/pub");


    }


    }
