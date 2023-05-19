package com.chatting.homebrewchat.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    @Column(name = "message_id")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private LocalDateTime time;
    private String text;

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom room;
    //TODO: 회원 ManyToOne
}
