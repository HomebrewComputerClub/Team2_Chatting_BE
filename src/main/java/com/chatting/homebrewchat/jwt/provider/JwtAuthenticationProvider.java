package com.chatting.homebrewchat.jwt.provider;

import com.chatting.homebrewchat.jwt.UserDetailsToken.Details;
import com.chatting.homebrewchat.jwt.UserDetailsToken.DetailsService;
import com.chatting.homebrewchat.jwt.util.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

// Provider 의 역할 : JwtToken 의 검증
// -> createToken(만들기, Access/refresh),
// -> parseToken(파싱, Access/refresh) ,
// -> 유저 ID 로부터 Token 얻기 (파싱하여 DB이용) ,
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider {

    private final RedisUtil redisUtil;

    @Value("${jwt.secretKey}")
    private byte[] accessSecret;

    @Value("${jwt.refreshKey}")
    private byte[] refreshSecret;

    public final static Long ACCESS_TOKEN_EXPIRE_COUNT = 180*2*30 * 1000L; // 30 seconds
    public final static Long REFRESH_TOKEN_EXPIRE_COUNT = 7 * 24 * 60 * 60 * 1000L; // 7 days , 밀리초 단위
    public final static Long REFRESH_TOKEN_EXPIRE_COUNT_REDIS = REFRESH_TOKEN_EXPIRE_COUNT/ 1000; // 1분, 초 단위

    @Autowired
    private DetailsService userDetailsService;

    /**
     * 적절한 설정을 통해 토큰을 생성하여 반환
     * @param authentication, byte[], Long
     * @return String
     */

    public String createToken(Authentication authentication, byte[] secret_key, Long expire_time) {

        Details userDetails= (Details) authentication.getPrincipal();
        Claims claims = Jwts.claims().setSubject(userDetails.getMember().getEmail()); // getName으로 sub에 이메일 넣기


        //토큰에 claims 추가 필요
        claims.put("memberID",userDetails.getMember().getMemberId());
        claims.put("name",userDetails.getMember().getName());
        claims.put("pic",userDetails.getMember().getPic());

        Date now = new Date();
        Date expiresIn = new Date(now.getTime() + expire_time);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiresIn)
                .signWith(SignatureAlgorithm.HS256, secret_key)
                .compact();
    }

    public String createAccessToken(Authentication authentication){

        return createToken(authentication,accessSecret, ACCESS_TOKEN_EXPIRE_COUNT);
    }

    public String createRefreshToken(Authentication authentication){
        return createToken(authentication, refreshSecret, REFRESH_TOKEN_EXPIRE_COUNT);
    }



    /**
     * 토큰이 서버에서 발행했는지, 만료시간이 되었는지를 검증 (create 할 때 만료시간 만들기)
     * @param (TokenString, secretkey)
     * @return Claims
     */
    public Claims getClaimsToken(String TokenString, byte[] secretkey) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretkey)
                //서버의 Secretkey와 대조, 실패하면 SignatureException 발생
                .build()
                .parseClaimsJws(TokenString)// Jws 객체형태로 돌려줌 + 만료 검사 수행
                // 만료일자 지나면 JwtException 발생
                .getBody(); // Claims 객체 반환

        return claims;

        // 여기까지 진행되면 아무 문제 없는 것이므로, return true;

    }


    public Claims getClaimsAccessToken(String accessTokenString){
        return getClaimsToken(accessTokenString, accessSecret);
    }
    public Claims getClaimsRefreshToken(String refreshTokenString){
        return getClaimsToken(refreshTokenString, refreshSecret);
    }

    /**
     * Member 객체에 접근하는 대신, UsernamepasswordAuthenticationToken 을 만들어 이용.
     * @param
     * @return Claims
     */

    public UsernamePasswordAuthenticationToken getAuthenticationToken(Claims claims){
        String email = claims.getSubject();
//        String role = claims.get("role", String.class);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken token
                = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        return token;
    }

//    public UsernamePasswordAuthenticationToken getAccessAuthenticationToke(Claims accessClaims){
//        return getAuthenticationToken(accessClaims);
//    }
//    public UsernamePasswordAuthenticationToken getRefreshAuthenticationToke(Claims refreshClaims){
//        return getAuthenticationToken(refreshClaims);
//    }





    public void setRefreshToken(String memberId, String refreshTokenString){
        log.info("RefreshToken 생성");
        redisUtil.setDataExpire(memberId, refreshTokenString, REFRESH_TOKEN_EXPIRE_COUNT_REDIS);
    }

    public String getRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals("RefreshToken"))
                        .map(Cookie::getValue))
                .findFirst()
                .orElse(null); // Refresh Token이 존재하지 않을 경우 null 반환 또는 적절한 예외 처리
    }

    /**
     * [리프레시 토큰 재발급 & DB에 리프레시 토큰 업데이트 메소드]
     * jwtService.createRefreshToken()으로 리프레시 토큰 재발급 후
     * DB에 재발급한 리프레시 토큰 업데이트 후 Flush
     */
    private String reIssueRefreshToken(String memberId, Authentication authentication) {
        String reIssuedRefreshToken = createRefreshToken(authentication);
        setRefreshToken(memberId, reIssuedRefreshToken);

        return reIssuedRefreshToken;
    }

    /**
     *  [리프레시 토큰으로 유저 정보 찾기 & 액세스 토큰/리프레시 토큰 재발급 메소드]
     *  파라미터로 들어온 헤더에서 추출한 리프레시 토큰으로 DB에서 유저를 찾고, 해당 유저가 있다면
     *  JwtService.createAccessToken()으로 AccessToken 생성,
     *  reIssueRefreshToken()로 리프레시 토큰 재발급 & DB에 리프레시 토큰 업데이트 메소드 호출
     *  그 후 JwtService.sendAccessTokenAndRefreshToken()으로 응답 헤더에 보내기
     */

    public String checkRefreshTokenAndReissuedToken(String Id, Authentication authentication){
        String memberId = redisUtil.getData(Id);
        //-> redis에 Id가 존재하지 않으면 RuntimeException 에러

        String reIssuedRefreshToken = reIssueRefreshToken(Id, authentication);
//        String reIssuedAccessToken = createAccessToken(authentication);

        return reIssuedRefreshToken;


    }
//
//    public Long getExpiration(String accessToken) {
//        // accessToken 남은 유효시간
//        Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody().getExpiration();
//        // 현재 시간
//        Long now = new Date().getTime();
//        return (expiration.getTime() - now);
//    }


}
