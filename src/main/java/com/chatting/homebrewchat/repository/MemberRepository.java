package com.chatting.homebrewchat.repository;

import com.chatting.homebrewchat.domain.dto.MemberInterface;
import com.chatting.homebrewchat.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
//    Member findByName(String name);
    @Query(value = "SELECT member_Id as id, name, email FROM member WHERE name like %:keyword% "
            , nativeQuery = true
    )
    List<MemberInterface> searchMemberContain(@Param("keyword") String keyword);

    Optional<Member> findFirstByName(String username);

}
