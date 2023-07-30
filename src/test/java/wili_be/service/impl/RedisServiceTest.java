package wili_be.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class RedisServiceTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> values; // ValueOperations를 모킹하기 위해 이 줄을 추가

    @InjectMocks
    private RedisServiceImpl redisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // redisTemplate의 opsForValue() 메서드가 모킹된 ValueOperations를 반환하도록 설정
        when(redisTemplate.opsForValue()).thenReturn(values);
    }
    @Test
    public void setValues() throws Exception {
        //given
        String key = "aba4647";
        String value = "refreshToken";
        when(values.get(key)).thenReturn(value); // MOCK의 동작을 명시적으로 정의해줘야 null을 반환 안 해준다.

        //when
        redisService.setValues(key, value);
        String refreshToken = redisService.getValues(key);

        //then
        assertEquals(value, refreshToken);
    }

    @Test
    public void getValues() throws Exception {

        //given
        String key = "aba4647";
        String value = "refreshToken";
        when(values.get(key)).thenReturn(value);

        //when
        String token = redisService.getValues(key);

        //then
        assertTrue(token != null && token.equals("refreshToken"));
    }
    @Test
    public void delValuesandexist() throws Exception {
        //given
        String key = "aba4647";
        String value = "refreshToken";
        when(values.get(key)).thenReturn(value);
        redisService.setValues(key, value);
        //when
        redisService.delValues(key);
        //then
        Boolean null_value = redisService.exists(key);
        assertFalse(null_value);
    }

        @Test
        public void setAccessTokenBlackList()throws Exception {
        //given
            String key = "aba4647";
            when(values.get(key)).thenReturn("BlackList");
            redisService.setAccessTokenBlackList(key);
        //when
            String blackList = redisService.getValues(key);
        //then
            assertEquals("BlackList", blackList);
        }

}
