package com.chatting.homebrewchat.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupChatRoom {
    @Id
    @Column(name = "group_room_id")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String title;
    @OneToOne
    @JoinColumn(name = "last_message_id")
    private ChatMessage lastMessage;

    @OneToMany(mappedBy = "groupChatRoom")
    private List<MemberGroupRoom> memberGroupRoomList=new ArrayList<>();

    @OneToMany(mappedBy = "groupChatRoom")
    private List<ChatMessage> messageList=new ArrayList<>();
    @Builder(builderMethodName = "init")
    public GroupChatRoom(String title){
        this.title=title;
    }

}
