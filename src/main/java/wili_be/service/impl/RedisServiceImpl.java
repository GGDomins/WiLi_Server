package wili_be.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import wili_be.service.RedisService;

import java.time.Duration;

// RedisSerivce
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    // 키-벨류 설정
    public void setValues(String key, String value){
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, value);
        redisTemplate.expire(key, Duration.ofHours(3)); // 3시간 후에 만료
    }


    // 키값으로 벨류 가져오기
    public String getValues(String key){
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        return values.get(key);
    }

    // 키-벨류 삭제
    public void delValues(String key) {
        redisTemplate.delete(key);
    } // <Key, Value> 쌍을 redis에서 지운다.
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    public void setAccessTokenBlackList(String accessTokenBlackList) {
        log.info(accessTokenBlackList);
        log.info("블랙리스트 토큰입니다.");
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        setValues(accessTokenBlackList, "BlackList");
        redisTemplate.expire(accessTokenBlackList, Duration.ofHours(3)); // 3시간 후에 만료
        String blackList = values.get(accessTokenBlackList);
        log.info(blackList);
        log.info("블랙리스트 완료");
    }

    public Boolean hasKeyBlackList(String AccessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(AccessToken));
    }
}