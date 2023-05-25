package com.chatting.homebrewchat.repository;

import com.chatting.homebrewchat.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member,String> {
}
