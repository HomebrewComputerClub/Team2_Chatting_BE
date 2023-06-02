package com.chatting.homebrewchat.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberGroupRoom {
    @Id
    @Column(name = "member_room_id")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;
    @ManyToOne
    @JoinColumn(name = "group_room_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GroupChatRoom groupChatRoom;
    @Builder(builderMethodName = "init")
    public MemberGroupRoom(Member member, GroupChatRoom groupChatRoom){
        this.member=member;
        this.groupChatRoom=groupChatRoom;
        groupChatRoom.getMemberGroupRoomList().add(this);
    }
}
