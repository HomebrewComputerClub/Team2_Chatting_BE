package com.chatting.homebrewchat.jwt.UserDetailsToken;

import com.chatting.homebrewchat.domain.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class Details implements UserDetails{ // , OAuth2User

    private Member member;
    private Map<String, Object> attributes;

    public Details(Member member){
        this.member = member;
    }
    public Details(Member member, Map<String, Object> attributes){
        this.member = member;
        this.attributes = attributes;
    }
//-----------------
//    // OAtuh 상속 메서드
//    @Override
//    public <A> A getAttribute(String name) {
////        return OAuth2User.super.getAttribute(name);
//        return null;
//    }
//
//
//    @Override
//    public String getName() {
//        return null;
//    }
//-----------------

    // 기본 UserDetail 상속 메서드

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return member.getRole().toString();
            }
        });
        return authorities;
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
