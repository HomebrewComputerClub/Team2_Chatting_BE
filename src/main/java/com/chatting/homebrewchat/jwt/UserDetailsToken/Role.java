package com.chatting.homebrewchat.jwt.UserDetailsToken;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role{

    ADMIN("관리자"), USER("일반 사용자");

    private final String title;

}
