package com.chatting.homebrewchat.repository;

import com.chatting.homebrewchat.domain.entity.ChatMessage;
import com.chatting.homebrewchat.domain.entity.ChatRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<ChatMessage,Long> {
    @EntityGraph(attributePaths = "sender")
    List<ChatMessage> findWithSenderByRoom(ChatRoom room);
}
