package wili_be.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wili_be.dto.MemberDto;
import wili_be.dto.MemberDto.Member_info_Dto;
import wili_be.entity.Member;
import wili_be.entity.Post;
import wili_be.repository.MemberRepository;
import wili_be.repository.ProductRepository;
import wili_be.service.AmazonS3Service;
import wili_be.service.MemberService;
import wili_be.service.ProductService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static wili_be.dto.MemberDto.*;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements UserDetailsService, MemberService {
    private final MemberRepository memberRepository;
    private final ProductService productService;
    private final AmazonS3Service amazonS3Service;

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
                .isBan(false)
                .isAdmin(false)
                .build();
        return memberRepository.save(member);
    }

    @Transactional
    public void removeMember(String snsId) {
        try {
            Optional<Member> memberOptional = findMemberById(snsId);
            Member member = memberOptional.get();

            List<String> imageKeys = productService.getImagesKeysByMember(member.getSnsId());
            if (imageKeys.isEmpty()) {
                memberRepository.delete(member);
            } else {
                amazonS3Service.deleteImagesByKeys(imageKeys);
                memberRepository.delete(member);
            }


        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Optional<Member> findMemberById(String sns_id) {
        return memberRepository.findBySnsId(sns_id);
    }

    @Override
    public String changeMemberInfoDtoToJson(Member_info_Dto memberInfoDto) {
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
