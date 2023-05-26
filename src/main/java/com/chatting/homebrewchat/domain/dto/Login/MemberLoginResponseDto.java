package com.chatting.homebrewchat.domain.dto.Login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
// ResponseDto 를 json으로 만들기 위해서 Getter 어노테이션이 필수,
// -> 쓰지 않을 경우 406에러 + Resolved [org.springframework.web.HttpMediaTypeNotAcceptableException: Could not find acceptable representation]
// 핸들러가 클라이언트가 요청한 Type으로 응답을 내려줄 수 없어 @Getter를 추가해주었더니 문제를 해결할 수 있었습니다.
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberLoginResponseDto {
    private String accessToken;
    private String refreshToken;

    private Long memberId;
    private String nickname;
}
