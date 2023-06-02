package com.chatting.homebrewchat.repository;

import com.chatting.homebrewchat.domain.entity.Member;
import com.chatting.homebrewchat.domain.entity.MemberGroupRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberGroupRoomRepository extends JpaRepository<MemberGroupRoom,String> {
    List<MemberGroupRoom> findWithGroupChatRoomByMember(Member member);
}
