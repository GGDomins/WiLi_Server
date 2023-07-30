package wili_be.service;

import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import wili_be.dto.MemberDto;
import wili_be.dto.MemberDto.Member_info_Dto;
import wili_be.entity.Member;

import java.util.Optional;
@Transactional
public interface MemberService{
    ResponseCookie createHttpOnlyCookie(String refreshToken);
    UserDetails loadUserByUsername(String snsId) throws UsernameNotFoundException;
    void saveIfNotExists(Member_info_Dto memberInfoDto);
    Optional<Member> findUserBySnsId(String sns_id);
    }