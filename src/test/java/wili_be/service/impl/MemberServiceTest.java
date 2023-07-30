package wili_be.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import wili_be.dto.MemberDto.Member_info_Dto;
import wili_be.entity.LoginProvider;
import wili_be.entity.Member;
import wili_be.repository.MemberRepository;
import wili_be.service.MemberService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
        memberDto.setName("John Doe");
        memberDto.setEmail("john@example.com");
        memberDto.setLoginProvider(LoginProvider.KAKAO);
        memberDto.setSnsId("google123");
    }

    @Test
    void saveIfNotExists_Should_SaveMember_When_MemberNotExists() {
        // Given
        when(memberRepository.findBySnsId(anyString())).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(new Member());

        // When
        memberService.saveIfNotExists(memberDto);

        // Then
        verify(memberRepository, times(1)).findBySnsId(memberDto.getSnsId());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void saveIfNotExists_Should_NotSaveMember_When_MemberExists() {
        // Given
        when(memberRepository.findBySnsId(anyString())).thenReturn(Optional.of(new Member()));

        // When
        memberService.saveIfNotExists(memberDto);

        // Then
        verify(memberRepository, times(1)).findBySnsId(memberDto.getSnsId());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void loadUserByUsername_Should_ReturnUserDetails_When_MemberExists() {
        // Given
        String snsId = "google123";
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
        String snsId = "google123";

        when(memberService.findUserBySnsId(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> memberService.loadUserByUsername(snsId));
    }
}
