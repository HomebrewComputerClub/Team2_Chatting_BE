package com.chatting.homebrewchat.config;

import com.chatting.homebrewchat.jwt.exception.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsUtils;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationManagerConfig authenticationManagerConfig;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private final AccessDeniedHandler jwtAccessDeniedHandler;

    // ManagerConfig 가 Manager 역할을 할 수 있게끔하고,
    // EntryPoint 가 에러가 일어났을 때, 어떤 것들을 할 수 있는지를 정해주도록 하기 위해서
    // -> 기본적인 세팅으로, 외워야 할 것들이 아닌 기본적인 방법들? 이라고 생각하면 될 듯

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // 서버에서 session 허용 X -> stateless

                // formLogin 과 basic 을 이용하지 않을으로써 기존 filter 의 역할을 사용하지 않을 것임.
                // 여기서는 UsernamePasswordFilter 의 기본 필터 대신 custom filter 적용시킬 것.
                // 그 이유는 JwtAuthenticationCustomFilter 에서 설명하겠음.

                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .csrf().disable() // 일단 csrf 비활성화인데, 귀찮아서 해놓은것이라 함.
                // 아마 customfilter 에서 OncePerRequestFilter 가 있기 때문에 csrf 공격을 막을 수 있을듯해서 그런 듯.
//                .cors()

                // AuthenticationManager 를 통해 기존 filter 대체
                .apply(authenticationManagerConfig) // configure 메서드를 통해 HttpSecurity 인 http 객체를 설정해줘야 함.


                // 접근 권한 설정 (pre-flight, cors설정)
                .and()
                .authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                // Preflight 요청은 허용한다. https://velog.io/@jijang/%EC%82%AC%EC%A0%84-%EC%9A%94%EC%B2%AD-Preflight-request

                .requestMatchers("/api/members/signup", "/api/members/login", "/api/members/refreshToken","/api/members/logout").permitAll()
                .requestMatchers("/api/manager/**").hasRole("ADMIN")
                //.requestMatchers("/api/manager로 시작하는 url") ADMIN 이 주어졌을 때 접근 허용 및 hasAnyRole 필요
                .anyRequest().authenticated()

                .and()
                .exceptionHandling()
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                // -> 인증되지 않은 사용자에 대한 접근처리
                .accessDeniedHandler(jwtAccessDeniedHandler)
                // -> 인증되었으나, 권한이 없는 사용자에 대한 처리
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }



}