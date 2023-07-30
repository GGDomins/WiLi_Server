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
/**
 * protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException: 이 메서드는 Filter 클래스의 doFilter 메서드를 오버라이드한 것입니다. 이 메서드는 실제로 필터가 수행하는 작업을 정의합니다. 매개변수로 HttpServletRequest, HttpServletResponse, FilterChain을 받습니다.
 *
 * String token = jwtTokenProvider.resolveToken((HttpServletRequest) request);: jwtTokenProvider 객체를 사용하여 요청 헤더에서 JWT 토큰을 추출합니다.
 *
 * if (token != null && jwtTokenProvider.validateToken(token)) { ... }: 추출한 JWT 토큰이 유효한지 확인합니다. 토큰이 유효하면 조건문 안의 코드 블록이 실행됩니다.
 *
 * Authentication authentication = jwtTokenProvider.getAuthentication(token);: 토큰으로부터 유저 정보를 추출하기 위해 jwtTokenProvider 객체의 getAuthentication 메서드를 호출합니다. 이 메서드는 JWT 토큰을 분석하여 해당 토큰에 대한 사용자 인증 정보를 생성합니다.
 *
 * SecurityContextHolder.getContext().setAuthentication(authentication);: 인증 정보를 SecurityContextHolder의 Context에 저장합니다. 이렇게 함으로써 Spring Security는 현재 사용자가 인증되었다고 인식할 수 있습니다.
 *
 * filterChain.doFilter(request, response);: 이 코드는 이 필터의 작업이 끝났음을 나타내고, 다음 필터로 요청을 전달합니다. 이 코드를 호출하지 않으면 요청이 더 이상 다음 필터나 서블릿으로 전달되지 않으며, 해당 요청에 대한 응답이 전송되지 않게 됩니다.
 */