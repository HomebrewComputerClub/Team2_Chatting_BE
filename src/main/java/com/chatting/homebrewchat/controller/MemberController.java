package com.chatting.homebrewchat.controller;

import com.chatting.homebrewchat.config.jwt.JwtFilter;
import com.chatting.homebrewchat.config.jwt.TokenProvider;
import com.chatting.homebrewchat.domain.dto.Login.MemberLoginDto;
import com.chatting.homebrewchat.domain.dto.Login.MemberLoginResponseDto;
import com.chatting.homebrewchat.domain.dto.MemberInterface;
import com.chatting.homebrewchat.domain.dto.Signup.MemberSignupDto;
import com.chatting.homebrewchat.domain.dto.Signup.MemberSignupResponseDto;
import com.chatting.homebrewchat.domain.entity.Member;
import com.chatting.homebrewchat.errorHandler.BaseException;
import com.chatting.homebrewchat.errorHandler.BaseResponseStatus;
import com.chatting.homebrewchat.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.chatting.homebrewchat.config.jwt.JwtFilter.AUTHORIZATION_HEADER;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")

public class MemberController {
    private final MemberService memberService;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "회원 로그아웃 처리")
    public ResponseEntity<String> logout(HttpServletResponse response){
        log.info("logout called");
        memberService.deleteRefresh();
        expireCookie(response,"RefreshToken");
        return new ResponseEntity<>("Logout Success", HttpStatus.OK);
    }
    private static void expireCookie(HttpServletResponse response,String name) {
        Cookie cookie=new Cookie(name, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody MemberSignupDto memberSignupDto){
        log.info("Got SignUp Signal");
        String encode = encoder.encode(memberSignupDto.getPassword());
        memberSignupDto.setPassword(encode);
        String join = memberService.join(memberSignupDto);
        return new ResponseEntity<>("User SignIn complete : "+join, null, HttpStatus.OK);
    }
    @PostMapping("/login")
    @Operation(summary = "Login", description = "로그인 성공 시 인증헤더에 접근토큰, 쿠키에 갱신토큰 심어준다.")
    public ResponseEntity<String> login(@RequestBody MemberLoginDto loginDTO, HttpServletResponse response){
        log.info("email: " + loginDTO.getEmail());
        String username = memberService.getUserNameByEmail(loginDTO.getEmail());
        log.info("User name: " + username);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, loginDTO.getPassword());
        try{
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            String accessJwt = tokenProvider.createToken(authentication,"Access");
            String refreshJwt = tokenProvider.createToken(authentication,"Refresh");

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(AUTHORIZATION_HEADER, "Bearer " + accessJwt);
            httpHeaders.add(JwtFilter.REFRESH_HEADER,"Bearer "+refreshJwt);
            log.info("in authenticate controller \nACCESS:{} \nREFRESH:{}",accessJwt,refreshJwt);


            memberService.setRefreshToken(username,refreshJwt);
            //FCM token 설정
            Cookie cookie = new Cookie("RefreshToken",refreshJwt);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            //cookie.setSecure(true);
            response.addCookie(cookie);

            return new ResponseEntity<>(accessJwt, httpHeaders, HttpStatus.OK);
        } catch (Exception e){
            System.out.println(e.getClass());
            System.out.println(e.getCause());
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
            throw new BaseException(BaseResponseStatus.INVALID_PASSWORD);
        }
    }

//    @GetMapping("/loginremain")
//    public void loginremain(HttpServletRequest request, HttpServletResponse response){
//
//        return;
//    }



    @GetMapping("/search/{keyword}")
    public List<MemberInterface> search(@PathVariable String keyword) {
        log.info("got signal in search");
        return memberService.searchMember(keyword);
    }
    @GetMapping("/tt")
    public String tt(){
        log.info("got ttttt");
        return "tt";
    }

}
