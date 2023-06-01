package com.chatting.homebrewchat.service;


import com.chatting.homebrewchat.domain.entity.Member;
import com.chatting.homebrewchat.domain.entity.MemberDetail;
import com.chatting.homebrewchat.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("userDetailsService")
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
   private final MemberRepository userRepository;

   public CustomUserDetailsService(MemberRepository userRepository) {
      this.userRepository = userRepository;
   }

   @Override
   @Transactional
   public UserDetails loadUserByUsername(final String username) {
      log.info("Loading user");
      Member user = userRepository.findFirstByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username + " Not Found"));
      return new MemberDetail(user);
   }


}
