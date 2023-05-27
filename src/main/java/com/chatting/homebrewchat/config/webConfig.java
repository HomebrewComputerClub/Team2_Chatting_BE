package com.chatting.homebrewchat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class webConfig implements WebMvcConfigurer {
    // cors 설정

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")
        //-> CORS를 적용할 URL 패턴을 지정합니다. 여기에서는 "/**" 패턴을 사용하여 모든 URL에 대해 적용하도록 설정합니다.
                .allowedOrigins("http://localhost:3000")
            .allowedOrigins("https://localhost:3000")
//                .allowedOrigins("http://localhost:8000")
                .allowedMethods("GET","POST","PATCH","DELETE","PUT","OPTIONS") // *로 지정해도 상관없을 듯.
                .allowCredentials(true);
        // 브라우저에서 CORS 요청을 보낼 때 쿠키를 함께 보내야 할 때, 이 메서드를 호출하여 true로 설정합니다.
    }
}
