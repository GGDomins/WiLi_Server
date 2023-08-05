package wili_be.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import wili_be.controller.status.StatusCode;
import wili_be.dto.MemberDto;
import wili_be.dto.TokenDto;
import wili_be.entity.Member;
import wili_be.security.JWT.JwtTokenProvider;
import wili_be.service.*;


import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static wili_be.dto.MemberDto.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final RedisService redisService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final TokenService tokenService;
    private final AmazonS3Service amazonS3Service;
    private final ProductService productService;
    private int StatusResult;

    @PostMapping("/users/auth")
    ResponseEntity<?> validateAccessToken(HttpServletRequest httpRequest) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            return createUnauthorizedResponse("접근 토큰이 없습니다");
        }

        StatusResult = tokenService.validateAccessToken(accessToken);

        if (StatusResult == StatusCode.UNAUTHORIZED) {
            return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
        }

        if (StatusResult != StatusCode.OK) {
            return createBadRequestResponse("잘못된 요청입니다");
        }
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
        Map<String, Object> response = new HashMap<>();
        response.put("snsId", snsId);
        return ResponseEntity.ok().body(response);
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

    @GetMapping("/users/{snsId}")
    ResponseEntity<String> getMemberInfo(HttpServletRequest httpRequest, @PathVariable String snsId) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            return createUnauthorizedResponse("접근 토큰이 없습니다");
        }
        StatusResult = tokenService.validateAccessToken(accessToken);
        if (StatusResult == StatusCode.UNAUTHORIZED) {
            return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
        }
        if (StatusResult != StatusCode.OK) {
            return createBadRequestResponse("잘못된 요청입니다");
        }
        Optional<Member> memberOptional = memberService.findMemberById(snsId);
        if (memberOptional.isPresent()) {
            MemberResponseDto memberResponseDto = new MemberResponseDto(memberOptional.get());
            String memberResponseDtoJson = memberService.changeMemberResponseDtoToJson(memberResponseDto);
            return ResponseEntity.ok().body(memberResponseDtoJson);
        } else {
            return ResponseEntity.badRequest().body("member가 존재하지 않습니다.");
        }
    }

    @PatchMapping("/users/{snsId}")
    public ResponseEntity<String> updateMember(HttpServletRequest httpRequest, @PathVariable String snsId, @RequestBody MemberUpdateRequestDto memberRequestDto) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            return createUnauthorizedResponse("접근 토큰이 없습니다");
        }
        StatusResult = tokenService.validateAccessToken(accessToken);

        if (StatusResult == StatusCode.UNAUTHORIZED) {
            return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
        }
        if (StatusResult != StatusCode.OK) {
            return createBadRequestResponse("잘못된 요청입니다");
        }
        try {
            MemberResponseDto memberResponseDto = memberService.updateMember(snsId, memberRequestDto);
            String updateMemberJson = memberService.changeMemberUpdateDtoToJson(memberResponseDto);
            return ResponseEntity.ok().body(updateMemberJson);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/users/{snsId}")
    public ResponseEntity<String> removeMember(HttpServletRequest httpServletRequest, @PathVariable String snsId) {
        String accessToken = jwtTokenProvider.resolveToken(httpServletRequest);

        if (accessToken == null) {
            return createUnauthorizedResponse("접근 토큰이 없습니다");
        }
        StatusResult = tokenService.validateAccessToken(accessToken);
        if (StatusResult == StatusCode.UNAUTHORIZED) {
            return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
        }
        if (StatusResult != StatusCode.OK) {
            return createBadRequestResponse("잘못된 요청입니다");
        }

        try {
            memberService.removeMember(snsId);
            List<String> imageKeys = productService.getImagesKeysByMember(snsId);
            amazonS3Service.deleteImagesByKeys(imageKeys);
            redisService.setAccessTokenBlackList(accessToken);
            return ResponseEntity.ok().body(snsId + "님이 탈퇴하셨습니다.");
        } catch (NullPointerException e) {
            memberService.removeMember(snsId);
            return ResponseEntity.ok().body(snsId + "님이 탈퇴하셨습니다.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.ok().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.ok().body(e.getMessage());
        }
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
