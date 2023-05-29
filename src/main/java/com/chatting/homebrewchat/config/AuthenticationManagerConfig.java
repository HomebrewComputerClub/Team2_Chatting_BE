package com.chatting.homebrewchat.config;

import com.chatting.homebrewchat.jwt.filter.JwtAuthenticationCustomFilter;
import com.chatting.homebrewchat.jwt.provider.JwtAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.authentication.AuthenticationProvider;
// AuthenticationProvider 이미 그런 클래스가 있기 때문에 앞에 Jwt 붙혀준 것임.

// Manager의 역할은 Filter 에서 manager.authenticate() 적용시키는 거 이외에는 할일 X
// 대부분의 Filter 역할들을 provider에서 진행하는데, provider 가 하나라면 굳이 Manager 를 적용할 필요 없다.
@Configuration
@RequiredArgsConstructor
public class AuthenticationManagerConfig extends AbstractHttpConfigurer<AuthenticationManagerConfig, HttpSecurity> {

    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);

        builder.addFilterBefore(new JwtAuthenticationCustomFilter(jwtAuthenticationProvider),
                UsernamePasswordAuthenticationFilter.class);
                // customfilter (Manager가 주입된) 를 Username~ Filter전에 실행시키겠다.

//                .authenticationProvider(jwtAuthenticationProvider);
                // 그 적용된 필터는 여러 provider 중에 내가 만든 jwtAuthenticationProvider 이다.
                // -> 결국 타고 들어가다보면 authenticationProviders.add(authenticationProvider); 이기 때문에
                    // Manager 가 여러개의 provider가 있더라도 관리할 수 있게끔 만들어주었다.

    }
}
