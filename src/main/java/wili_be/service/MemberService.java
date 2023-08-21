package wili_be.service;

import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import wili_be.dto.MemberDto;
import wili_be.dto.MemberDto.Member_info_Dto;
import wili_be.entity.Member;

import java.util.List;
import java.util.Optional;

import static wili_be.dto.MemberDto.*;

public interface MemberService{
    ResponseCookie createHttpOnlyCookie(String refreshToken);
    UserDetails loadUserByUsername(String snsId) throws UsernameNotFoundException;
    Optional<Member> findMemberById(String sns_id);
    Member saveUser(AdditionalSignupInfo memberDto);
    void removeMember(String Id);
    MemberResponseDto updateMember(String snsId, MemberUpdateRequestDto memberRequestDto);
    boolean validateExistingMember(String username);
    MemberResponseDto findMemberByUserName(String username);
    }
