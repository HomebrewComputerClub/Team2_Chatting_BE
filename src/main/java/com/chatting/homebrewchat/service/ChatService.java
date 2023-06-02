package com.chatting.homebrewchat.service;

import com.chatting.homebrewchat.domain.dto.ChatDto;
import com.chatting.homebrewchat.domain.dto.DirectMessageDto;
import com.chatting.homebrewchat.domain.dto.MessageType;
import com.chatting.homebrewchat.domain.entity.*;
import com.chatting.homebrewchat.errorHandler.BaseException;
import com.chatting.homebrewchat.errorHandler.BaseResponseStatus;
import com.chatting.homebrewchat.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
    private final GroupChatRoomRepository groupChatRoomRepository;
    private final MemberGroupRoomRepository memberGroupRoomRepository;

    private Member getMember(Long id){
        return memberRepository.findById(id).orElseThrow(()->new RuntimeException("no mem"));
    }
    @Transactional
    public String getDirectRoomId(Long id){
//        if(req.getMyMemberId().equals(req.getTargetMemberId())){
//            throw new RuntimeException("same MemberID..");
//        }
        log.info("in service");
        Member me = memberService.getMember();

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
        Member me=memberService.getMember();
        List<ChatRoom> byMemberA = roomRepository.findByMemberA(me);
        List<ChatRoom> byMemberB = roomRepository.findByMemberB(me);
        List<ChatDto.roomListRes> result = new ArrayList<>();
        result.addAll(byMemberA.stream()
                .map(cr->ChatDto.roomListRes.builder().roomId(cr.getId()).targetName(cr.getMemberB().getName())
                        .targetImage(cr.getMemberB().getPic()).targetMemberId(cr.getMemberB().getMemberId())
                        .lastContent(cr.getLastMessage()==null?"아직 아무 대화를 나누지 않았어요":cr.getLastMessage().getText()).lastSendTime(cr.getLastMessage()==null?LocalDateTime.now():cr.getLastMessage().getTime()).build())
                .collect(Collectors.toList()));
        result.addAll(byMemberB.stream().map(cr->ChatDto.roomListRes.builder().roomId(cr.getId()).targetName(cr.getMemberA().getName())
                        .targetImage(cr.getMemberA().getPic()).targetMemberId(cr.getMemberA().getMemberId())
                        .lastContent(cr.getLastMessage()==null?"아직 아무 대화를 나누지 않았어요":cr.getLastMessage().getText()).lastSendTime(cr.getLastMessage()==null?LocalDateTime.now():cr.getLastMessage().getTime()).build()
                ).collect(Collectors.toList()));
        Collections.sort(result, new Comparator<ChatDto.roomListRes>() {
            @Override
            public int compare(ChatDto.roomListRes o1, ChatDto.roomListRes o2) {
                return o2.getLastSendTime().compareTo(o1.getLastSendTime());
            }
        });
        return result;
    }
    @Transactional
    public void saveChatMessage(DirectMessageDto msg){
        Member currentMember;
        try {
            currentMember = memberService.getMember();
            log.info("Find Member By Security");
        }catch (Exception e){
            log.info("Cannot Find Member");
        }finally {
            currentMember=memberRepository.findById(msg.getMemberId()).orElseThrow(()->new BaseException(BaseResponseStatus.INVALID_USERID));
        }

        Optional<ChatRoom> byId = roomRepository.findById(msg.getRoomId());
        if(!byId.isPresent()){
            throw new RuntimeException("no room");
        }
        ChatMessage build = ChatMessage.init().text(msg.getDetail()).room(byId.get()).sender(currentMember).build();
        messageRepository.save(build);
        byId.get().setLastMessage(build);
    }
    public List<DirectMessageDto> getDirectMessageList(String roomId){
        ChatRoom room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("roomID Nono"));
        List<ChatMessage> msgs = messageRepository.findWithSenderByRoom(room);
        if(msgs.isEmpty()){
            return Collections.singletonList(DirectMessageDto.builder().build());
        }
        Collections.sort(msgs, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage o1, ChatMessage o2) {
                return o1.getTime().compareTo(o2.getTime());
            }
        });
        List<DirectMessageDto> messageInfoList = msgs.stream().map(m -> DirectMessageDto.builder().detail(m.getText())
                .senderName(m.getSender().getName()).messageId(m.getId()).memberId(m.getSender().getMemberId())
                .roomId(roomId).type(MessageType.SEND).build()
        ).collect(Collectors.toList());
        return messageInfoList;
    }

    @Transactional
    public String createGroupRoom(String title) {
        Member member = memberService.getMember();
        GroupChatRoom groupChatRoom = GroupChatRoom.init().title(title).build();
        MemberGroupRoom build = MemberGroupRoom.init().member(member).groupChatRoom(groupChatRoom).build();
        groupChatRoomRepository.save(groupChatRoom);
        memberGroupRoomRepository.save(build);
        return groupChatRoom.getId();
    }
    public List<ChatDto.groupRoomRes> getMyGroupRoomList(){
        Member member = memberService.getMember();
        List<MemberGroupRoom> groupRoomList = memberGroupRoomRepository.findWithGroupChatRoomByMember(member);
        return groupRoomList.stream().map(r->ChatDto.groupRoomRes.builder().roomId(r.getGroupChatRoom().getId())
                .roomTitle(r.getGroupChatRoom().getTitle()).targetImage(member.getPic())
                .lastContent(r.getGroupChatRoom().getLastMessage().getText())
                .lastSendTime(r.getGroupChatRoom().getLastMessage().getTime()).build()
        ).collect(Collectors.toList());
    }

    public List<DirectMessageDto> getGroupMessageList(String roomId) {
        GroupChatRoom groupChatRoom = groupChatRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("invalid group room id"));
        List<ChatMessage> messageList = messageRepository.findWithSenderByGroupChatRoom(groupChatRoom);
        Collections.sort(messageList, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage o1, ChatMessage o2) {
                return o1.getTime().compareTo(o2.getTime());
            }
        });
        return messageList.stream().map(m->DirectMessageDto.builder().detail(m.getText())
                .senderName(m.getSender().getName()).messageId(m.getId()).memberId(m.getSender().getMemberId())
                .roomId(roomId).type(MessageType.SEND).build()
        ).collect(Collectors.toList());

    }
    @Transactional
    public void saveGroupMessage(DirectMessageDto message) {
        Member currentMember=memberRepository.findById(message.getMemberId()).orElseThrow(()->new BaseException(BaseResponseStatus.INVALID_USERID));
        Optional<GroupChatRoom> byId = groupChatRoomRepository.findById(message.getRoomId());
        if(!byId.isPresent()){
            throw new RuntimeException("no room");
        }
        ChatMessage build = ChatMessage.groupInit().text(message.getDetail()).groupChatRoom(byId.get()).sender(currentMember).build();
        messageRepository.save(build);
        byId.get().setLastMessage(build);
    }

    @Transactional
    public void inviteMemberToGroup(Long memberId, String roomId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_USERID));
        GroupChatRoom groupChatRoom = groupChatRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("invalid group room id"));
        MemberGroupRoom build = MemberGroupRoom.init().member(member).groupChatRoom(groupChatRoom).build();
        memberGroupRoomRepository.save(build);
    }
}
