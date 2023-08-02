package wili_be.service;

import org.springframework.transaction.annotation.Transactional;
import wili_be.dto.TokenDto;

import javax.servlet.http.HttpServletRequest;
public interface TokenService {
    TokenDto createTokens(String snsId);
    TokenDto createTokensFromRefreshToken(String snsId, String old_refreshToken);
    int validateAccessToken(String accessToken);

}
