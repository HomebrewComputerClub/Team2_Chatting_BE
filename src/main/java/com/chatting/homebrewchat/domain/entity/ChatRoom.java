package com.chatting.homebrewchat.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {
    @Id
    @Column(name = "room_id")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String title;
    @OneToOne
    @JoinColumn(name = "last_message_id")
    private ChatMessage lastMessage;

    @ManyToOne
    @JoinColumn(name = "member_a_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member memberA;
    @ManyToOne
    @JoinColumn(name = "member_b_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member memberB;

    @OneToMany(mappedBy = "room")
    private List<ChatMessage> messageList;

    @Builder(builderMethodName = "init")
    public ChatRoom(String title, Member memberA, Member memberB){
        this.title="default";
        this.memberA=memberA;
        this.memberB=memberB;
    }
}
