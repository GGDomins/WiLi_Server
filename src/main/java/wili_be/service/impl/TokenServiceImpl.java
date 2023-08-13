package wili_be.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import wili_be.controller.status.StatusCode;
import wili_be.dto.TokenDto;
import wili_be.exception.CustomExceptions;
import wili_be.security.JWT.JwtTokenProvider;
import wili_be.security.JWT.TokenType;
import wili_be.service.RedisService;
import wili_be.service.TokenService;

import javax.servlet.http.HttpServletRequest;

import static wili_be.exception.CustomExceptions.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final Long AccessexpireTimeMs = 1000 * 60 * 60 * 60L; // 3시간
    private final Long RefreshExpireTimeMs = 2L * 24 * 60 * 60 * 1000L; // 2 days in milliseconds

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    @Override
    public TokenDto createTokens(String snsId) {
        TokenDto tokenDto = new TokenDto();
        String accessToken = jwtTokenProvider.createToken(snsId, TokenType.ACCESS_TOKEN.getValue(), AccessexpireTimeMs);
        String refreshToken = jwtTokenProvider.createToken(snsId, TokenType.REFRESH_TOKEN.getValue(), RefreshExpireTimeMs);

        redisService.setValues(refreshToken, snsId);

        tokenDto.setAccessToken(accessToken);
        tokenDto.setRefreshToken(refreshToken);

        return tokenDto;
    }

    @Override
    public TokenDto createTokensFromRefreshToken(String snsId, String old_refreshToken) {
        TokenDto tokenDto = new TokenDto();
        String accessToken = jwtTokenProvider.createToken(snsId, TokenType.ACCESS_TOKEN.getValue(), AccessexpireTimeMs);
        String refreshToken = jwtTokenProvider.createToken(snsId, TokenType.REFRESH_TOKEN.getValue(), RefreshExpireTimeMs);

        redisService.setValues(refreshToken, snsId);
        redisService.delValues(old_refreshToken);
        tokenDto.setAccessToken(accessToken);
        tokenDto.setRefreshToken(refreshToken);

        return tokenDto;
    }

    @Override
    public void validateAccessToken(String accessToken) {
        if (redisService.hasKeyBlackList(accessToken)) {
            throw new NotLoggedInException();
        } else if (jwtTokenProvider.validateToken(accessToken)) {

        } else if (jwtTokenProvider.isTokenExpired(accessToken)) {
            throw new ExpiredTokenException();
        } else {
            throw new BadRequestException();
        }
    }

}
