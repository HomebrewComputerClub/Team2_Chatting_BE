package com.chatting.homebrewchat.service;

import com.chatting.homebrewchat.domain.dto.MemberInterface;
import com.chatting.homebrewchat.domain.dto.Signup.MemberSignupDto;
import com.chatting.homebrewchat.domain.entity.Member;
import com.chatting.homebrewchat.errorHandler.BaseException;
import com.chatting.homebrewchat.errorHandler.BaseResponseStatus;
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
//@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
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
    public Member getMember() {
        String userName = SecurityUtil.getCurrentUsername().orElseThrow(() -> new BaseException(BaseResponseStatus.FAILED_TO_LOGIN));
        Member user = memberRepository.findFirstByUsername(userName).orElseThrow(() -> new BaseException(BaseResponseStatus.FAILED_TO_LOGIN));
        return user;
    }

    @Transactional
    public void deleteRefresh() {
        Member user = getMember();
        user.setRefreshToken("");
    }

    @Transactional
    public String join(MemberSignupDto memberSignupDto) {
        if(memberRepository.findFirstByEmail(memberSignupDto.getEmail()).orElse(null)!=null)
            throw new BaseException(BaseResponseStatus.POST_USERS_EXISTS_EMAIL);
        Member member = Member.builder().email(memberSignupDto.getEmail()).password(memberSignupDto.getPassword())
                .name(memberSignupDto.getName()).build();
        memberRepository.save(member);
        return memberSignupDto.getName();

    }
    public String getUserNameByEmail(String email) {
        Member member = memberRepository.findFirstByEmail(email).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_EMAIL));
        return member.getUsername();
    }
    @Transactional
    public void setRefreshToken(String username,String refreshJwt) {
        Member member = memberRepository.findFirstByUsername(username).orElseThrow(() -> new BaseException(BaseResponseStatus.FAILED_TO_LOGIN));
        member.setRefreshToken(refreshJwt);
    }
}
