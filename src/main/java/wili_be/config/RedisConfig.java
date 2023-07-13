package wili_be.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableRedisRepositories
@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}") // yml에 host 정의
    private String redisHost;

    @Value("${spring.redis.port}") // yml에 port 정의
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Value("${spring.redis.ssl}")
    private boolean useSsl;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisHost);
        redisStandaloneConfiguration.setPort(redisPort);
        redisStandaloneConfiguration.setPassword(redisPassword);

        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration);
        lettuceConnectionFactory.setUseSsl(useSsl);

        return lettuceConnectionFactory;
    }
}
