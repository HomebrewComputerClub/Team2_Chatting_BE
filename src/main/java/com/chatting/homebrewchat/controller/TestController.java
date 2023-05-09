package com.chatting.homebrewchat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {
    @GetMapping("/stringTest")
    public String stringTest(){
        return "Hello, world! halo";
    }
    @GetMapping("/hello")
    public String stringTest(){
        return "new hello..";
    }
}
