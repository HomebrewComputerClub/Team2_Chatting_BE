package com.chatting.homebrewchat.controller;

import com.chatting.homebrewchat.domain.dto.Login.MemberLoginDto;
import com.chatting.homebrewchat.domain.dto.Login.MemberLoginResponseDto;
import com.chatting.homebrewchat.domain.dto.MemberInterface;
import com.chatting.homebrewchat.domain.dto.Signup.MemberSignupDto;
import com.chatting.homebrewchat.domain.dto.Signup.MemberSignupResponseDto;
import com.chatting.homebrewchat.domain.entity.Member;
import com.chatting.homebrewchat.jwt.UserDetailsToken.Details;
import com.chatting.homebrewchat.jwt.UserDetailsToken.DetailsService;
import com.chatting.homebrewchat.jwt.UserDetailsToken.Role;
import com.chatting.homebrewchat.jwt.provider.JwtAuthenticationProvider;
import com.chatting.homebrewchat.jwt.util.RedisUtil;
import com.chatting.homebrewchat.service.MemberService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")

public class MemberController {
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final DetailsService DetailsService;
    private final RedisUtil redisUtil;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody @Valid MemberSignupDto memberSignupDto, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            log.error("binding result 에러");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        // 이미 존재하는 이메일인지 검증
        if( memberService.IsPresentEmail(memberSignupDto.getEmail()) ){
            log.error("이미 존재하는 이메일인 에러");
            return new ResponseEntity(HttpStatus.CONFLICT); // 에러 409, 이미 있는 이메일.
        }

        // @Builder 로 바꿀 수도 있음.
        Member member = new Member();
        member.setName(memberSignupDto.getName());
        member.setEmail(memberSignupDto.getEmail());
        member.setPassword(passwordEncoder.encode(memberSignupDto.getPassword()));
        if(memberSignupDto.getPic() != null){
            member.setPic(memberSignupDto.getPic());
        }
        member.setRole(Role.ADMIN);

        Member saveMember = memberService.addMember(member);
        // Spring Data Jpa 통해 MemberRepository.save 할 수도 있지만,
        // 추가로 role 권한을 설정해줘야 하기 때문에 Service에서 수행
        // -> 물론 MemberRepository.save 하기만 해도 Service로 분리하는 것이 좋음.


        // 테스트용 response가 잘 받아오는지 -> responsedto 객체를 json 으로 바꿔주는 @ResponseBody
        MemberSignupResponseDto memberSignupResponseDto = new MemberSignupResponseDto();
        memberSignupResponseDto.setMemberId(saveMember.getMemberId());
        memberSignupResponseDto.setEmail(saveMember.getEmail());
        memberSignupResponseDto.setPassword(saveMember.getPassword());
        // Member 객체가 만들어지면서 필드의 regdate 가 자동 생성됨.
        memberSignupResponseDto.setRegdate(saveMember.getRegdate());

        // 회원가입
        return new ResponseEntity(HttpStatus.OK);
//memberSignupResponseDto,

    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid MemberLoginDto memberLoginDto, BindingResult bindingResult, HttpServletResponse response){
        if(bindingResult.hasErrors()){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        // ----모두 일치하는 경우-----
        UsernamePasswordAuthenticationToken token
                = new UsernamePasswordAuthenticationToken(memberLoginDto.getEmail(), memberLoginDto.getPassword());
        // 로그인할 때는 email과 password로만 이루어진 token 만들기
        log.info("token 생성");
        // DB에 id가 있는지, DB의 id와 pw가 일치하는지까지 모두 검사해줌
        // excepthon -> 추가바람

        // 검증되지 않은 토큰 = authentication
        Authentication authentication = token;

        // 토큰 검증
        try{
            authentication = authenticationManagerBuilder.getObject().authenticate(token);
        }
        catch (BadCredentialsException e){ // 일치하지 않는 Id, Pw
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        log.info("authentication 생성");
        if (authentication.isAuthenticated()) {
            System.out.println("인증성공");
        } else {
            // 인증 실패
            System.out.println("인증실패");
            // 에러 응답 처리
        }
        //모두 일치하면 access, refreshToken 발급
        // 인증 토큰 발급할 때,
        String accessToken = jwtAuthenticationProvider.createAccessToken(authentication);
        String refreshToken = jwtAuthenticationProvider.createRefreshToken(authentication);

        // RefreshToken을 DB에 저장한다. 성능 때문에 DB가 아니라 Redis에 저장하는 것이 좋다.
        // 그리고 redis에 저장하면서 사용자의 Id 를 같이 저장한다.
        //Repository에서 Member를 가져와도 되지만, User~Token을 검사한 authentication의 userdetails에 모든 회원정보가 들어가 있음.
//        Member member = memberService.findByEmail(memberLoginDto.getEmail());

        System.out.println("pt1");
        Details userDetails= (Details) authentication.getPrincipal();
        Member member = userDetails.getMember();
//        System.out.println(member.toString());
        System.out.println("pt1.5555");
        jwtAuthenticationProvider.setRefreshToken(String.valueOf(member.getMemberId()), refreshToken);
        // Id는 long 형태이므로
        System.out.println("pt22");
        // AccessToken은 헤더의 Authorization 의 Barrer 뒤에 토큰을 붙혀 return 시키기.
        HttpHeaders httpHeaders = memberService.setHeaderAccessToken(accessToken);
        // RefreshToken은 브라우저의 쿠키에 지정하여 보낸다.
        memberService.setCookieRefreshToken(response, refreshToken);


        // 테스트용 잘되었는지
        MemberLoginResponseDto loginResponse = MemberLoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .memberId(member.getMemberId())
                .nickname(member.getName())
                .build();

        System.out.println(loginResponse);

        log.info(loginResponse.getAccessToken());

        return new ResponseEntity( httpHeaders, HttpStatus.OK);
//        return new ResponseEntity<>(accessToken, httpHeaders, HttpStatus.OK);
        // 헤더에 acceessToken, 쿠키에 ResponseToken 담아서, body에는 테스트용
//        loginResponse,
    }

    @GetMapping("/loginremain")
    public void loginremain(HttpServletRequest request, HttpServletResponse response){

        return;
    }

    @PostMapping("/logout") // Redis DB 삭제 = 끝
    public ResponseEntity logout(HttpServletRequest request, HttpServletResponse response){
        log.info("logout 시작");
        // 저장된 인증객체를 SecurityContextHolder에서 꺼냄
        SecurityContext securityContext = SecurityContextHolder.getContext();
        log.info(securityContext.toString());

        // 인증객체로부터 Id 추출
        Authentication authentication = securityContext.getAuthentication();
        if(authentication != null){
            try{
                Details userDetails = (Details) authentication.getPrincipal();
                String memberId = String.valueOf(userDetails.getMember().getMemberId());
                log.info(memberId);

                // Id와 일치하는 DB(redis)에 존재하는 key 삭제
                memberService.checkAndDeleteRefresh(memberId);
                // 찾지 못할 시, RuntimeException 발생

                // 클라이언트에 만료된 쿠키 전달 -> 쿠키삭제
                memberService.setExpireCookie(response,"RefreshToken");
                log.info(memberId);
            } catch (ClassCastException e){
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
            // 리다이렉트 시킴으로써 private변수의 AccessToken 없애기
//            String redirect_uri="http://www.google.com";
//            response.sendRedirect(redirect_uri);
        }

        // 인증객체를 없애기 => SecurityContext 를 clear()
        // 사실 웹 어플리케이션에서는 stateless 로 이루어지기 때문에 굳이 안해도 되는데 특수 상황에선 써야한다고 함.

        securityContext.setAuthentication(null);
        log.info(securityContext.toString());
        log.info(SecurityContextHolder.getContext().toString());
        return new ResponseEntity(HttpStatus.OK);
//        "Logout Success",
    }

    @GetMapping("/search/{member}")
    public List<MemberInterface> search(@PathVariable("keyword") String keyword) {
        return memberService.searchMember(keyword);
    }
    @GetMapping("/getUsername")
    public String usernameTest(){
        memberService.getCurrentMember();
        return "End";
    }



}
