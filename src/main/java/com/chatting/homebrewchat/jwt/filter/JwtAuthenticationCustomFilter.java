package com.chatting.homebrewchat.jwt.filter;

// 이 필터에서는 UsernamePasswordAuthenticationFilter 이전에 처리되어야할 customFilter
// 헤더의 Authorization 부분의 Barrer 부분을 제외한 AccessToken 부분을 받아서
// 주입받은 Manager를 통해 Authenticate() 하여 맞는 AccessToken 인지 인증하는 필터 적용

import com.chatting.homebrewchat.jwt.provider.JwtAuthenticationProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


// -> 왜 CustomFilter를 만들었냐? = 로그인폼 아니고 JWT 방식으로 로그인 활용하려고,
// -> 그럼 첨부터 CustomFilter 하지, 왜 Username~ Filter 쓰냐? = Username~ Filter를 이용한 기본적으로는 사용하기 위해서
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationCustomFilter extends OncePerRequestFilter {

    private final JwtAuthenticationProvider provider;
    // 원래 같으면 util 의 Tokenizer 클래스에 UsernameToken을 만들기까지의 과정을 넣고,
    // 이 클래스에서는 Manager.authenticate() 만 하면 완성될 수 있도록 하면 더 좋다.

    // Filter는 Access토큰이 존재할 때만 사용되는 것.

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("새로운 필터 시작");
        String accessTokenString = resolveToken(request);
        String requestURI = request.getRequestURI();

        if (accessTokenString != null){
            try{
                Claims claimsAccess = provider.getClaimsAccessToken(accessTokenString);
                log.info("accessToken 만료 안됨");
                log.info("claimsAccess : " + claimsAccess);
                // secretkey 불일치 시, SignatureException 발생
                // 토큰 기한 만료 시, ExpiredJwtException

                    UsernamePasswordAuthenticationToken token = provider.getAuthenticationToken(claimsAccess);
                    // -> Manager.authenticate 기능을 모두 수행해버려서 authenticate 필요없음
                    // sub에 암호화된 데이터를 집어넣고, 복호화하는 코드를 넣어줄 수 있다.
                    //(여긴안한거)

                    // 인증정보가 SecurityContextHolder 에 저장되게 됨
                    SecurityContextHolder.getContext()
                            .setAuthentication(token); // 현재 요청에서 언제든지 인증정보를 꺼낼 수 있도록 해준다.
                    log.info("SecurityContextHolder" + SecurityContextHolder.getContext());

            }catch (ExpiredJwtException e){

                log.info("Access토큰 만료 후");
                // 쿠키의 "RefreshToken" 으로부터 RefreshToke 추출
                String refreshTokenString = provider.getRefreshToken(request);
                Claims claimsRefresh = provider.getClaimsRefreshToken(refreshTokenString);
                log.info("claimsRefresh : " + claimsRefresh);
                // secretkey 인증 -> 실패 시 SignatureException
                // 토큰 만료 인증 -> 실패 시 JwtException -> 다시 로그인 알림


                UsernamePasswordAuthenticationToken token = provider.getAuthenticationToken(claimsRefresh);

                //redis 에 refreshToken 저장
                String ReissuedRefreshToken = provider.
                        checkRefreshTokenAndReissuedToken(String.valueOf(claimsRefresh.get("memberID")), token);
                // claims의 Id 가 DB(redis) 에 존재 X -> RuntimeException 에러
//
                    String ReissuedAccessToken = provider.createAccessToken(token);

                    // 인증정보가 SecurityContextHolder 에 저장되게 됨
                    SecurityContextHolder.getContext()
                            .setAuthentication(token); // 현재 요청에서 언제든지 인증정보를 꺼낼 수 있도록 해준다.
                    log.info("SecurityContextHolder" + SecurityContextHolder.getContext());

                if( !requestURI.equals("/members/logout") ){
                    // Filter에서는 바로 헤더의 Authorization의 Bearer 뒤에 AccessToken 보내기
                    response.setHeader("Authorization","Bearer "+ReissuedAccessToken);
                    log.info("ReissuedAccessToken : " + ReissuedAccessToken);

                    // RefreshToken은 브라우저의 쿠키에 지정하여 보낸다.
                    Cookie cookie = new Cookie("RefreshToken",ReissuedRefreshToken);
                    cookie.setHttpOnly(true);
                    cookie.setPath("/");
                    // cookie.setSecure(true); //-> https에서만 가능하게
                    response.addCookie(cookie);
                }
            }
        }
        filterChain.doFilter(request, response);
        }


    /**
     * http 헤더로부터 bearer 토큰을 가져옴.
     * @param request
     * @return
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null; // 아무것도 받지 못했을 때, ( header에 아무것도 없을 때 )
    }
}
