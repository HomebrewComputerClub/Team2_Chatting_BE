package com.chatting.homebrewchat.config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
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
@RequiredArgsConstructor
public class ChatConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
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
        registry.enableSimpleBroker("/direct", "/multi");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new AuthChannelInterceptorAdapter());
    }

    private class AuthChannelInterceptorAdapter implements ChannelInterceptor {

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String token = accessor.getFirstNativeHeader("Authorization");
                // Validate and authenticate the token here
                // You can use your JWT token validation logic here

                // If the token is valid, set the user in the security context
                // This assumes you have a custom UserDetailsService implementation for loading user details
                // based on the token
                log.info("------------token : ----------- :{} ",token);
           //     UsernamePasswordAuthenticationToken authentication = authenticate(token);
          //      accessor.setUser(authentication);
            }

            return message;
        }

//        private UsernamePasswordAuthenticationToken authenticate(String token) {
//            // Implement your token validation and authentication logic here
//            // You can use libraries like jjwt or Nimbus JOSE + JWT for token validation
//            // Once validated, create and return an Authentication object based on the token
//
//            // Example using a custom UserDetailsService to load user details based on the token
//
//
//
//            UserDetails userDetails = .loadUserByToken(token);
//            return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
//        }
    }
}
