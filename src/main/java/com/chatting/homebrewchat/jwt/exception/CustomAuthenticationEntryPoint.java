package com.chatting.homebrewchat.jwt.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 예외처리를 위한 클래스
@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
//        log.error("가입되지 않은 사용자 접근");
//        response.sendRedirect("/login");

        // 자세한 에러를 보고 싶을 때 사용
//        authException.printStackTrace();
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED); // 401에러
    }

    // 만료 에러, key 인증 에러

}
