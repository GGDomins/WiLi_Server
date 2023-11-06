package wili_be.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wili_be.dto.MemberDto;
import wili_be.dto.MemberDto.Member_info_Dto;
import wili_be.dto.PostDto;
import wili_be.entity.Member;
import wili_be.entity.Post;
import wili_be.exception.CustomExceptions;
import wili_be.repository.MemberRepository;
import wili_be.repository.ProductRepository;
import wili_be.service.MemberService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static wili_be.dto.MemberDto.*;
import static wili_be.exception.CustomExceptions.*;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements UserDetailsService, MemberService {
    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public Member saveUser(AdditionalSignupInfo memberDto) {
        Member member = Member.builder()
                .name(memberDto.getName())
                .email(memberDto.getEmail())
                .loginProvider(memberDto.getLoginProvider())
                .snsId(memberDto.getSnsId())
                .username(memberDto.getUsername())
                .birthday(memberDto.getBirthday())
                .favorites(memberDto.getFavorites())
                .isBan(false)
                .isAdmin(false)
                .build();
        return memberRepository.save(member);
    }

    public boolean validateExistingMember(String username) {
        Optional<Member> memberOptional = memberRepository.findMemberByUsername(username);
        if (memberOptional.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Member findMemberByMemberName(String username) {
        Member member = memberRepository.findMemberByUsername(username).orElseThrow(() -> new UsernameNotFoundException("user을 찾을 수 없습니다."));
        return member;
    }

    @Override
    public void chechMemberIdExist(MemberSignupDto memberSignupDto) {
        Optional<Member> member = memberRepository.findMemberByEmail(memberSignupDto.getEmail());
        if (member.isPresent()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 동일한 email이 존재합니다.");
        }
    }

    @Override
    public void saveMember(MemberSignupDto memberSignupDto) {
        Member member = memberSignupDto.of();
        memberRepository.save(member);
    }

    @Transactional
    @Override
    public void removeMember(String snsId) {
        Member member = findMemberById(snsId).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "user을 찾을 수 없습니다."));
        memberRepository.delete(member);
    }

    @Transactional
    @Override
    public MemberResponseDto updateMember(String snsId, MemberUpdateRequestDto memberRequestDto) {
        Member member = findMemberById(snsId).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "user을 찾을 수 없습니다."));
        // 요청으로 받은 필드들로 업데이트
        if (memberRequestDto.getName() != null) {
            member.setName(memberRequestDto.getName());
        }
        if (memberRequestDto.getEmail() != null) {
            member.setEmail(memberRequestDto.getEmail());
        }
        if (memberRequestDto.getLoginProvider() != null) {
            member.setLoginProvider(memberRequestDto.getLoginProvider());
        }
        if (memberRequestDto.getUsername() != null) {
            member.setUsername(memberRequestDto.getUsername());
        }
        if (memberRequestDto.getBirthday() != null) {
            member.setBirthday(memberRequestDto.getBirthday());
        }
        if (memberRequestDto.getFavorites() != null) {
            member.setFavorites(memberRequestDto.getFavorites());
        }
        memberRepository.save(member);
        MemberResponseDto memberUpdateResponseDto = new MemberResponseDto(member);
        return memberUpdateResponseDto;

    }

    @Override
    public Optional<Member> findMemberById(String sns_id) {
        return memberRepository.findBySnsId(sns_id);
    }

    @Override
    public ResponseCookie createHttpOnlyCookie(String refreshToken) {
        //HTTPONLY 쿠키에 RefreshToken 생성후 전달
        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(3600000)
                .build();

        return responseCookie;
    }

    @Override
    public UserDetails loadUserByUsername(String snsId) throws UsernameNotFoundException {
        return memberRepository.findBySnsId(snsId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
