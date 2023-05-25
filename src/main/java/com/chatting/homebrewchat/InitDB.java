package com.chatting.homebrewchat;

import com.chatting.homebrewchat.domain.entity.Member;
import com.chatting.homebrewchat.repository.MemberRepository;
import com.chatting.homebrewchat.repository.RoomRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitDB {
    private final MemberRepository memberRepository;
    @PostConstruct
    public void init(){
        Member member = new Member("usera", "testA", "AA", "123");
        Member member2 = new Member("userb", "testB", "BB", "123");
        Member member3 = new Member("userc", "testC", "CC", "123");
        memberRepository.save(member);
        memberRepository.save(member2);
        memberRepository.save(member3);
    }
}
