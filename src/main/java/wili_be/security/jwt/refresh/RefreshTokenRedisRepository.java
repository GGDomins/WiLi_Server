package wili_be.security.jwt.refresh;

import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRedisRepository extends CrudRepository<RefreshToken, Long> {
    RefreshToken findByRefreshToken(String refreshToken);
}
