package com.chatting.homebrewchat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            web.ignoring().requestMatchers("/css/**","/js/**","/images/**");
        };
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration source = new CorsConfiguration();
        source.setAllowCredentials(false); //쿠키 받을 것 인지 설정
        source.setAllowedOrigins(Arrays.asList("http://localhost:3000","https://localhost:3000","http://localhost:8080"));
        source.setAllowedMethods(Arrays.asList("GET"));
        source.addAllowedMethod("POST");
        source.addAllowedMethod("PUT");
        source.addAllowedMethod("DELETE");
        source.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource url= new UrlBasedCorsConfigurationSource();
        url.registerCorsConfiguration("/**", source);

        return url;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.authorizeHttpRequests().requestMatchers("/**").permitAll()
                .anyRequest().permitAll().and()
                .formLogin().disable().httpBasic().disable()
                .csrf().disable().cors(Customizer.withDefaults());
        return http.build();
    }
}
