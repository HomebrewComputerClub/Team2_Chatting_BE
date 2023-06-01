package com.chatting.homebrewchat.service;

import com.chatting.homebrewchat.domain.dto.MemberInterface;
import com.chatting.homebrewchat.domain.entity.Member;
import com.chatting.homebrewchat.jwt.util.RedisUtil;
import com.chatting.homebrewchat.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final RedisUtil redisUtil;

    @Transactional
    public Member findByEmail(String email){
        return memberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다."));
    }

    @Transactional
    public boolean IsPresentEmail(String email){
        Optional<Member> member= memberRepository.findByEmail(email);
        if(member.isPresent()){
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public Member addMember(Member member){
        return memberRepository.save(member);
    }

    @Transactional
    public HttpHeaders setHeaderAccessToken(String accessToken){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization","Bearer "+accessToken);
        return httpHeaders;
    }

    @Transactional
    public void setCookieRefreshToken(HttpServletResponse response, String refreshToken){
//        Cookie cookie = new Cookie("RefreshToken",refreshToken);
//        cookie.setHttpOnly(true);
//        cookie.setPath("/");
//        cookie.setSecure(true); //-> https에서만 가능하게
//        response.addCookie(cookie);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("RefreshToken", refreshToken)
                .path("/")
                .sameSite("None")
                .httpOnly(true)
                .secure(true)
                .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

    }

    @Transactional
    public void checkAndDeleteRefresh(String Id){
        String memberId = redisUtil.getData(Id);
        // 찾지 못할 시, RuntimeException 발생
        redisUtil.deleteData(Id);
    }

    @Transactional
    public void setExpireCookie(HttpServletResponse response,String name) {
//        Cookie cookie=new Cookie(name, null);
//        cookie.setMaxAge(0);
//        cookie.setSecure(true); //-> https에서만 가능하게
//        cookie.setHttpOnly(true);
//        cookie.setPath("/");
//        response.addCookie(cookie);
        ResponseCookie refreshTokenCookie = ResponseCookie.from(name, null)
                .path("/")
                .sameSite("None")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }
    public List<MemberInterface> searchMember(String keyword){
        List<MemberInterface> memberList = memberRepository.searchMemberContain(keyword);
        return memberList;
    }
    public Member getCurrentMember(){
        String username = SecurityUtil.getCurrentUsername().orElseThrow(() -> new RuntimeException("Not Match username"));
        log.info("Got Username: "+username);
        Member member = memberRepository.findFirstByName(username).orElseThrow(() -> new RuntimeException("Cannot find member"));
        return member;
    }
}
