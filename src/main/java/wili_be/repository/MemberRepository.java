package wili_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wili_be.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findMemberBySnsId(String snsId);
    Optional<Member> findBySnsId(String snsId);
    Optional<Member> findMemberByUsername(String username);
    Optional<Member> findMemberByEmail(String email);
}
