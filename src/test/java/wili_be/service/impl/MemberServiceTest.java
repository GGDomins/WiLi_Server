package wili_be.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import wili_be.dto.MemberDto.*;
import wili_be.entity.LoginProvider;
import wili_be.entity.Member;
import wili_be.repository.MemberRepository;
import wili_be.service.MemberService;
import wili_be.service.impl.MemberServiceImpl;

import java.util.Optional;

public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member_info_Dto memberDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 테스트 시작 전에 Member_info_Dto 객체를 초기화
        memberDto = new Member_info_Dto();
        memberDto.setName("kevin");
        memberDto.setEmail("kevin0928@naver.com");
        memberDto.setLoginProvider(LoginProvider.KAKAO);
        memberDto.setSnsId("qwer1234");
    }

    @Test
    void saveUser_Should_ReturnSavedMember_When_ValidInputProvided() {
        // Given
        AdditionalSignupInfo additionalSignupInfo = AdditionalSignupInfo.builder()
                .name(memberDto.getName())
                .email(memberDto.getEmail())
                .loginProvider(memberDto.getLoginProvider())
                .snsId(memberDto.getSnsId())
                .build();

        // Mock the behavior of the memberRepository.save() method
        Member savedMember = new Member();
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // When
        Member result = memberService.saveUser(additionalSignupInfo);

        // Then
        assertNotNull(result);
        assertEquals(savedMember, result);
    }

    @Test
    void findUserBySnsId_Should_ReturnMember_When_MemberExists() {
        // Given
        String snsId = "qwer1234";
        Member member = memberDto.to_Entity();
        when(memberRepository.findBySnsId(anyString())).thenReturn(Optional.of(member));

        // When
        Optional<Member> result = memberService.findUserBySnsId(snsId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(member, result.get());
    }

    @Test
    void findUserBySnsId_Should_ReturnEmptyOptional_When_MemberNotExists() {
        // Given
        String snsId = "non_existent_sns_id";
        when(memberRepository.findBySnsId(anyString())).thenReturn(Optional.empty());

        // When
        Optional<Member> result = memberService.findUserBySnsId(snsId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void changeToJson_Should_ReturnJsonString_When_ValidInputProvided() {
        // Given

        // When
        String json = memberService.changeToJson(memberDto);

        // Then
        assertNotNull(json);
        // Add more assertions if needed to check the contents of the JSON string
    }

    @Test
    void createHttpOnlyCookie_Should_ReturnResponseCookie_When_ValidInputProvided() {
        // Given
        String refreshToken = "sample_refresh_token";

        // When
        ResponseCookie cookie = memberService.createHttpOnlyCookie(refreshToken);

        // Then
        assertNotNull(cookie);
        // Add more assertions if needed to check the contents of the ResponseCookie object
    }

    @Test
    void loadUserByUsername_Should_ReturnUserDetails_When_MemberExists() {
        // Given
        Member member = memberDto.to_Entity();
        when(memberRepository.findBySnsId(anyString())).thenReturn(Optional.of(member));

        // When
        UserDetails userDetails = memberService.loadUserByUsername(member.getSnsId());

        // Then
        assertNotNull(userDetails);
        assertEquals(member.getUsername(), userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_Should_ThrowException_When_MemberNotExists() {
        // Given
        String snsId = "qwer1234";
        when(memberRepository.findBySnsId(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> memberService.loadUserByUsername(snsId));
    }
}
