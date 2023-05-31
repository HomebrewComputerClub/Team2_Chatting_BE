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
    private final MemberService memberService;

    private Member getMember(Long id){
        return memberRepository.findById(id).orElseThrow(()->new RuntimeException("no mem"));
    }
    @Transactional
    public String getDirectRoomId(Long id){
//        if(req.getMyMemberId().equals(req.getTargetMemberId())){
//            throw new RuntimeException("same MemberID..");
//        }
        log.info("in service");
        Member me = memberService.getCurrentMember();
//        Member me = getMember(req.getMyMemberId());
        Member target = getMember(id);
        List<ChatRoom> byMemberA = roomRepository.findByMemberA(me);
        List<ChatRoom> byMemberB = roomRepository.findByMemberB(me);
        Optional<ChatRoom> optA = byMemberA.stream().filter(c->c.getMemberB().equals(target)).findAny();
        Optional<ChatRoom> optB = byMemberB.stream().filter(c->c.getMemberA().equals(target)).findAny();
        log.info("hello");
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
    public List<ChatDto.roomListRes> getMyRoomList(){
//        Member me=getMember(memberId);
        Member me=memberService.getCurrentMember();
        List<ChatRoom> byMemberA = roomRepository.findByMemberA(me);
        List<ChatRoom> byMemberB = roomRepository.findByMemberB(me);
        List<ChatDto.roomListRes> result = new ArrayList<>();
//        result.addAll(byMemberA.stream()
//                .map(cr->ChatDto.roomListRes.builder().roomId(cr.getId()).targetName(cr.getMemberB().getName())
//                        .targetImage(cr.getMemberB().getPic()).targetMemberId(cr.getMemberB().getMemberId())
//                        .lastContent(cr.).build())
//                .collect(Collectors.toList()));
//        result.addAll(byMemberB.stream().map(ChatRoom::getId).collect(Collectors.toList()));
        return result;
    }
    @Transactional
    public void saveChatMessage(DirectMessageDto msg){
        Member currentMember = memberService.getCurrentMember();
        Optional<ChatRoom> byId = roomRepository.findById(msg.getRoomId());
        if(!byId.isPresent()){
            throw new RuntimeException("no room");
        }
        ChatMessage build = ChatMessage.init().text(msg.getDetail()).room(byId.get()).sender(currentMember).build();
        messageRepository.save(build);
    }
    public List<DirectMessageDto> getDirectMessageList(String roomId){
        List<ChatMessage> msgs = messageRepository.findWithSenderByRoom(roomId);
        return msgs.stream().map(m-> DirectMessageDto.builder().detail(m.getText())
                .roomId(m.getRoom().getId()).senderName(m.getSender().getName()).messageId(m.getId()).build()
        ).collect(Collectors.toList());
    }

}
