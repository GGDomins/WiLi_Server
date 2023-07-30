package wili_be.security.JWT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import wili_be.entity.Member;
import wili_be.service.impl.MemberServiceImpl;

import javax.servlet.http.HttpServletRequest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    private String secretKey = "annaannaannaannaannaannaanna";
    private static final String static_snsID = "12312312312312312312312adsf";
    private static final Long AccessexpireTimeMs = 600000l; // 5분
    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private MemberServiceImpl memberService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // openMocks을 이용해 Mock어노테이션으로 주석된 필드를 초기화 한다.
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", secretKey);
    }
    @Test
    public void createAccessToken ()throws Exception {
        //given
        String accessToken = jwtTokenProvider.createToken(static_snsID, TokenType.ACCESS_TOKEN.getValue(), AccessexpireTimeMs);

        //when

        //then
        assertNotNull(accessToken);
    }
    @Test
    public void getAuthentication()throws Exception {
        //given
        String accessToken = jwtTokenProvider.createToken(static_snsID, TokenType.ACCESS_TOKEN.getValue(), AccessexpireTimeMs);
        Member member = mock(Member.class);
        //when
        when(memberService.loadUserByUsername(static_snsID)).thenReturn(member);
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        //then
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
    }
    @Test
    public void getUserId() throws Exception {
        //given
        String accessToken = jwtTokenProvider.createToken(static_snsID, TokenType.ACCESS_TOKEN.getValue(), AccessexpireTimeMs);
        //when
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
        //then
        assertEquals(snsId,static_snsID);
    }
    @Test
    public void getExpiration() throws Exception {
        //given
        Date date = new Date();
        Long nowDate = date.getTime();
        String accessToken = jwtTokenProvider.createToken(static_snsID, TokenType.ACCESS_TOKEN.getValue(), AccessexpireTimeMs);
        //when

        //then
        Long ActualTime = jwtTokenProvider.getExpiration(accessToken) / 1000;
        Long expectedTime = (nowDate + AccessexpireTimeMs) / 1000;
        /**
         * 실제 시간과 토큰이 만들어지는 시간을 고려해 1000으로 숫자를 나눴다. -> 밀리초가 아닌 초 단위로 표현하게 하기 위해서
         * 토큰이 만들어지는 시간을 1초 이내로 고려한 결과임
         */
        //then
        assertEquals(expectedTime, ActualTime);
    }
    @Test
    public void resolveToken() throws Exception {
        //given
        String accessToken = jwtTokenProvider.createToken(static_snsID, TokenType.ACCESS_TOKEN.getValue(), AccessexpireTimeMs);
        String requestToken = "Bearer " + accessToken;
        //when
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(requestToken);
        //then
        String resolvedToken = jwtTokenProvider.resolveToken(request);
        assertEquals(requestToken.substring(7), resolvedToken);
    }
    @Test
    public void validateToken()throws Exception {
        //given
        String accessToken = jwtTokenProvider.createToken(static_snsID, TokenType.ACCESS_TOKEN.getValue(), AccessexpireTimeMs);
        //when

        //then
        assertTrue(jwtTokenProvider.validateToken(accessToken));
    }
    @Test
    public void isTokenExpired()throws Exception {
        //given
        String accessToken = jwtTokenProvider.createToken(static_snsID, TokenType.ACCESS_TOKEN.getValue(), AccessexpireTimeMs);

        //when

        //then
        assertFalse(jwtTokenProvider.isTokenExpired(accessToken));
    }


}