package com.chatting.homebrewchat.service;

import com.chatting.homebrewchat.domain.dto.ChatDto;
import com.chatting.homebrewchat.domain.dto.DirectMessageDto;
import com.chatting.homebrewchat.domain.entity.ChatMessage;
import com.chatting.homebrewchat.domain.entity.ChatRoom;
import com.chatting.homebrewchat.domain.entity.Member;
import com.chatting.homebrewchat.repository.MemberRepository;
import com.chatting.homebrewchat.repository.MessageRepository;
import com.chatting.homebrewchat.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;

    private Member getMember(Long id){
        return memberRepository.findById(id).orElseThrow(()->new RuntimeException("no mem"));
    }
    @Transactional
    public String getDirectRoomId(ChatDto.makeRoomReq req){
        if(req.getMyMemberId().equals(req.getTargetMemberId())){
            throw new RuntimeException("same MemberID..");
        }

        Member me = getMember(req.getMyMemberId());
        Member target = getMember(req.getTargetMemberId());
        List<ChatRoom> byMemberA = roomRepository.findByMemberA(me);
        List<ChatRoom> byMemberB = roomRepository.findByMemberB(me);
        Optional<ChatRoom> optA = byMemberA.stream().filter(c->c.getMemberB().equals(target)).findAny();
        Optional<ChatRoom> optB = byMemberB.stream().filter(c->c.getMemberA().equals(target)).findAny();
        if(optA.isPresent()&&optB.isPresent()){
            //error
            throw new RuntimeException("Check DB..");
        }
        if(optA.isPresent()||optB.isPresent()){
            //이미 만들어져있다.
            return optA.isPresent()?optA.get().getId():optB.get().getId();
        }
        ChatRoom build = ChatRoom.init().memberA(me).memberB(target).build();
        log.info("me name:{}",build.getMemberA().getName());
        log.info("target name:{}",build.getMemberB().getName());
        roomRepository.save(build);
        return build.getId();
    }
    public ChatDto.roomListRes getMyRoomList(Long memberId){
        Member me=getMember(memberId);
        List<ChatRoom> byMemberA = roomRepository.findByMemberA(me);
        List<ChatRoom> byMemberB = roomRepository.findByMemberB(me);
        List<String> result = new ArrayList<>();
        result.addAll(byMemberA.stream().map(ChatRoom::getId).collect(Collectors.toList()));
        result.addAll(byMemberB.stream().map(ChatRoom::getId).collect(Collectors.toList()));
        return new ChatDto.roomListRes(result);
    }
    @Transactional
    public void saveChatMessage(DirectMessageDto msg){
        // TODO : getMember로 회원 정보 얻어야 함
        Optional<ChatRoom> byId = roomRepository.findById(msg.getRoomId());
        if(!byId.isPresent()){
            throw new RuntimeException("no room");
        }
        ChatMessage build = ChatMessage.init().text(msg.getDetail()).room(byId.get()).build();
        messageRepository.save(build);
    }
    public List<DirectMessageDto> getDirectMessageList(String roomId){
        // TODO: 현재 회원 정보 얻어야 함..
        List<ChatMessage> msgs = messageRepository.findWithSenderByRoom(roomId);
        return msgs.stream().map(m-> DirectMessageDto.builder().detail(m.getText())
                .roomId(m.getRoom().getId()).senderName("After Auth").messageId(m.getId()).build()
        ).collect(Collectors.toList());
    }

}
