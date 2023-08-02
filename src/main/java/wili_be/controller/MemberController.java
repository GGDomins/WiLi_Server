package wili_be.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import wili_be.controller.status.StatusCode;
import wili_be.dto.TokenDto;
import wili_be.entity.Member;
import wili_be.security.JWT.JwtTokenProvider;
import wili_be.service.MemberService;
import wili_be.service.RedisService;
import wili_be.service.TokenService;


import javax.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final RedisService redisService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final TokenService tokenService;
    private int StatusResult;
    @PostMapping("/users/auth")
    ResponseEntity<String> validateAccessToken(HttpServletRequest httpRequest) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            return createUnauthorizedResponse("접근 토큰이 없습니다");
        }

        StatusResult = tokenService.validateAccessToken(accessToken);

        if (StatusResult == StatusCode.UNAUTHORIZED) {
            return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
        }

        if (StatusResult == StatusCode.OK) {
            return ResponseEntity.ok().body("정상적인 접근입니다");
        }
        return createBadRequestResponse("잘못된 요청입니다");
    }


    private ResponseEntity<String> createUnauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", "not-logged-in")
                .body(message);
    }

    private ResponseEntity<String> createExpiredTokenResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", "Bearer error=\"invalid_token\"")
                .body(message);
    }

    private ResponseEntity<String> createBadRequestResponse(String message) {
        return ResponseEntity.badRequest().body(message);
    }


    @PostMapping("/users/refresh-token")
    ResponseEntity<String> validateRefreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        log.info(refreshToken);
        if (redisService.exists(refreshToken)) {
            String snsId = redisService.getValues(refreshToken);
            log.info(snsId);
            log.info("refreshToken 값 추출");
            TokenDto newToken = tokenService.createTokensFromRefreshToken(snsId, refreshToken);
            ResponseCookie responseCookie = memberService.createHttpOnlyCookie(newToken.getRefreshToken());
            String new_accessToken = newToken.getAccessToken();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .header("accessToken", new_accessToken)
                    .body("create accessToken from refreshToken finish");
        }
        return new ResponseEntity<>("there is no RefreshToken in redis", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/users/logout")
    ResponseEntity<String> logout(HttpServletRequest request) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        redisService.setAccessTokenBlackList(accessToken);
        log.info("logout");
        return ResponseEntity.ok("set" + accessToken + "blackList");
    }

    @DeleteMapping("/users/delete/{Id]")
    public ResponseEntity<String> removeMember(@PathVariable String Id) {
        try {
            memberService.removeMember(Id);
            return ResponseEntity.ok().body(Id + "님이 탈퇴하셨습니다.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 회원입니다.");
        }
    }
}
