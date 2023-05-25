package com.chatting.homebrewchat.repository;

import com.chatting.homebrewchat.domain.entity.ChatRoom;
import com.chatting.homebrewchat.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<ChatRoom,String> {
    List<ChatRoom> findByMemberA(Member memberA);
    List<ChatRoom> findByMemberB(Member memberB);
}
