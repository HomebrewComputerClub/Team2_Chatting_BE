package com.chatting.homebrewchat.repository;

import com.chatting.homebrewchat.domain.entity.GroupChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom,String> {
}
