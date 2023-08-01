package wili_be.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import wili_be.dto.MemberDto;
import wili_be.dto.MemberDto.Member_info_Dto;
import wili_be.entity.Member;
import wili_be.repository.MemberRepository;
import wili_be.service.MemberService;

import java.util.Optional;

import static wili_be.dto.MemberDto.*;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements UserDetailsService, MemberService {
    private final MemberRepository memberRepository;

    public Member saveUser(AdditionalSignupInfo memberDto) {
        Member member = Member.builder()
                .name(memberDto.getName())
                .email(memberDto.getEmail())
                .loginProvider(memberDto.getLoginProvider())
                .snsId(memberDto.getSnsId())
                .username(memberDto.getUsername())
                .birthday(memberDto.getBirthday())
                .isBan(false)
                .isAdmin(false)
                .build();
        return memberRepository.save(member);
    }

    public Optional<Member> findUserBySnsId(String sns_id) {
        return memberRepository.findBySnsId(sns_id);
    }

    @Override
    public String changeToJson(Member_info_Dto memberInfoDto) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String memberDtoJson = objectMapper.writeValueAsString(memberInfoDto);
            return memberDtoJson;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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
