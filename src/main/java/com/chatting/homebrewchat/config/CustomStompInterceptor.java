package com.chatting.homebrewchat.config;

import com.chatting.homebrewchat.config.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import static com.chatting.homebrewchat.config.jwt.JwtFilter.AUTHORIZATION_HEADER;
import static com.chatting.homebrewchat.config.jwt.JwtFilter.REFRESH_HEADER;

@Slf4j
public class CustomStompInterceptor implements ChannelInterceptor {
    private TokenProvider tokenProvider;
    public CustomStompInterceptor(TokenProvider tokenProvider){
        this.tokenProvider=tokenProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String header = accessor.getFirstNativeHeader("Authorization");
        log.info("in interceptor");
        if(header!=null){
            log.info("Got header : "+header);
            String token = resolveToken(header);
            Authentication authentication = tokenProvider.getAuthentication(token,"ACCESS");
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        return message;
    }
    private String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
