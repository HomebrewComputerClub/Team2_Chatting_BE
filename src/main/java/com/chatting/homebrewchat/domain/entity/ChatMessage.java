package com.chatting.homebrewchat.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChatRoom room;
    @ManyToOne
    @JoinColumn(name = "group_room_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GroupChatRoom groupChatRoom;
    @ManyToOne
    @JoinColumn(name = "sender_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member sender;
    @Builder(builderMethodName = "init")
    public ChatMessage(String text, ChatRoom room, Member sender){
        this.text=text;
        this.room=room;
        this.sender=sender;
        this.time=LocalDateTime.now();
    }
    @Builder(builderMethodName = "groupInit")
    public ChatMessage(String text, GroupChatRoom groupChatRoom, Member sender){
        this.text=text;
        this.groupChatRoom=groupChatRoom;
        this.sender=sender;
        this.time=LocalDateTime.now();
        groupChatRoom.getMessageList().add(this);
    }

}
