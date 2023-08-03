package wili_be.security.JWT;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.filter.OncePerRequestFilter;
import wili_be.controller.status.StatusCode;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//해당 클래스는 JwtTokenProvider가 검증을 끝낸 Jwt로부터 유저 정보를 조회해와서 UserPasswordAuthenticationFilter 로 전달합니다.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;
    private int StatusResult;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtTokenProvider.resolveToken(request);

        if (accessToken == null) {
            ResponseEntity<String> unauthorizedResponse = createUnauthorizedResponse("접근 토큰이 없습니다");
            response.setStatus(unauthorizedResponse.getStatusCodeValue());
            response.getWriter().write(unauthorizedResponse.getBody());
            return;
        } else {
            StatusResult = validateAccessToken(accessToken);
            if (StatusResult == StatusCode.UNAUTHORIZED) {
                ResponseEntity<String> expiredTokenResponse = createExpiredTokenResponse("접근 토큰이 만료되었습니다");
                response.setStatus(expiredTokenResponse.getStatusCodeValue());
                response.getWriter().write(expiredTokenResponse.getBody());
                return;
            } else if (StatusResult == StatusCode.OK) {
                ResponseEntity<String> okResponse = ResponseEntity.ok().body("정상적인 접근입니다");
                response.setStatus(okResponse.getStatusCodeValue());
                response.getWriter().write(okResponse.getBody());
                return;
            } else {
                ResponseEntity<String> badRequestResponse = createBadRequestResponse("잘못된 요청입니다");
                response.setStatus(badRequestResponse.getStatusCodeValue());
                response.getWriter().write(badRequestResponse.getBody());
                return;
            }
        }
    }

    private int validateAccessToken(String accessToken) {
        if (hasKeyBlackList(accessToken)) {
            return StatusCode.BAD_REQUEST;
        }
        else if (jwtTokenProvider.validateToken(accessToken)) {
            return StatusCode.OK;
        }
        else if (jwtTokenProvider.isTokenExpired(accessToken)) {
            return StatusCode.UNAUTHORIZED;
        } else {
            return StatusCode.BAD_REQUEST;
        }
    }
    private Boolean hasKeyBlackList(String AccessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(AccessToken));
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
}
