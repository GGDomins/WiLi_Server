package wili_be.service;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface RedisService {
    void setValues(String key, String value);

    String getValues(String key);

    void delValues(String key);

    boolean exists(String key);

    void setAccessTokenBlackList(String accessTokenBlackList);

    Boolean hasKeyBlackList(String AccessToken);
}
