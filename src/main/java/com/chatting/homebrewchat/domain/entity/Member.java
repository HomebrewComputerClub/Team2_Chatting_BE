package com.chatting.homebrewchat.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@ToString
@Table(name = "member")
@NoArgsConstructor // -> 이거 때문에 에러 났었음.
@Setter
@Getter
public class Member {
    @Id
    @Column(name = "member_Id") // memberId로 했을 때는 적용되지 않더라. 스네이크 표현형식 고정인듯.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

//    @Column(unique = true)
    private String username;
    private String email;

    private String name;

//    @Builder.Default()
    private String pic = "https://res.cloudinary.com/dql4ynp7j/image/upload/v1683891387/mtkc8k2miuzbawzquxt5.jpg";

    @JsonIgnore
    private String password;

    @CreationTimestamp
    private LocalDateTime regdate;

    @Enumerated(EnumType.STRING)
    // 엔티티 필드에 저장된 Enum 값을 문자열 형태로 데이터베이스에 저장
    private MemberRole role;

    // Oauth 관련--------------------

//    // oauth 관련
    private String provider; //어떤 OAuth인지(google, naver 등)
    private String refreshToken;

    @Column(name = "provide_id")
    private String provideId; // 해당 OAuth 의 key(id)
    @Builder
    public Member(String name, String password, String email, String provider, String provideId) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.role = MemberRole.USER;
        this.provider = provider;
        this.provideId = provideId;
        this.regdate = LocalDateTime.now();
        this.username=email.split("@")[0];
    }
}
