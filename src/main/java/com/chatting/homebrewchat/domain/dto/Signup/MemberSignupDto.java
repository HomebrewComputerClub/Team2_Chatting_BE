package com.chatting.homebrewchat.domain.dto.Signup;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSignupDto {

//    @NotEmpty // 띄어쓰기는 허용
//    @Pattern(regexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
//            message = "이메일 형식을 맞춰야합니다")
    private String email;

//    @NotEmpty
//    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{7,16}$",
//            message = "비밀번호는 영문+숫자+특수문자를 포함한 8~20자이어야 합니다.")
    @Setter
    private String password;

//    @NotEmpty
//    @Pattern(regexp = "^[a-zA-Zㄱ-ㅎ가-힣\\\\s]{2,15}$",
//            message = "이름은 영문자, 한글, 공백포함 2글자부터 15글자까지 가능합니다.")
    private String name;
    private String pic;

//    @NotEmpty
//    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",
//            message = "전화번호 형태가 맞지 않습니다.")
//    private String phonenum;

    // 만약 이미 가입한 회원일 경우, admin 권한 주기
    // -> 본인 기수 기입
}
