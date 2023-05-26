package com.chatting.homebrewchat.domain.entity;

import com.chatting.homebrewchat.jwt.UserDetailsToken.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@ToString
@Table(name = "member")
@NoArgsConstructor
@Setter
@Getter
public class Member {
    @Id
    @Column(name = "member_Id") // memberId로 했을 때는 적용되지 않더라. 스네이크 표현형식 고정인듯.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

//    @Column(unique = true)
    private String email;

    private String name;

    @JsonIgnore
    private String password;

    @CreationTimestamp
    private LocalDateTime regdate;

    @Enumerated(EnumType.STRING)
    // 엔티티 필드에 저장된 Enum 값을 문자열 형태로 데이터베이스에 저장
    private Role role;

}
