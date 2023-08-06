package wili_be.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static wili_be.dto.PostDto.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import wili_be.dto.MemberDto.*;
import wili_be.dto.PostDto;
import wili_be.entity.LoginProvider;
import wili_be.entity.Member;
import wili_be.entity.Post;
import wili_be.repository.MemberRepository;
import wili_be.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private MemberServiceImpl memberService;
    private Member_info_Dto memberDto;
    private PostInfoDto postInfoDto;
    private List<Post> posts = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 테스트 시작 전에 Member_info_Dto 객체를 초기화
        memberDto = new Member_info_Dto();
        memberDto.setName("kevin");
        memberDto.setEmail("kevin0928@naver.com");
        memberDto.setLoginProvider(LoginProvider.KAKAO);
        memberDto.setSnsId("qwer1234");
        //PostInfo 객체를 초기화
        postInfoDto = new PostInfoDto();
        postInfoDto.setBrandName("나이키");
        postInfoDto.setProductName("범고래");
        postInfoDto.setCategory("신발");
        postInfoDto.setProductPrice("200");
        postInfoDto.setDescription("범고래 신발 너무 흔해요");
        postInfoDto.setLink("www.nike.com");
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
        Optional<Member> result = memberService.findMemberById(snsId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(member, result.get());
    }

    @Test
    void removeMember_Should_DeleteMember_When_MemberExists() {
        // Given
        String snsId = "qwer1234";
        Member existingMember = memberDto.to_Entity();
        Post post = postInfoDto.to_Entity();
        List<Post> postList = new ArrayList<>();
        postList.add(post);
        existingMember.setPosts(postList);
        when(memberRepository.findBySnsId(snsId)).thenReturn(Optional.of(existingMember));

        // When
        memberService.removeMember(snsId);
        // Then
        verify(memberRepository, times(1)).delete(existingMember);
        verify(productRepository, times(1)).delete(post);
        //assertNull(existingMember); 삭제하는 것은 null로 변환하는 것이 아니다. 단지 db에서 없앨 뿐이지.
    }

    @Test
    void loadUserByUsername_Should_ReturnUserDetails_When_MemberExists() {
        // Given
        String snsId = "qwer1234";
        Member member = memberDto.to_Entity();
        when(memberRepository.findBySnsId(anyString())).thenReturn(Optional.of(member));

        // When
        UserDetails userDetails = memberService.loadUserByUsername(snsId);

        // Then
        assertNotNull(userDetails);
        assertEquals(member.getUsername(), userDetails.getUsername());
    }
}
