package wili_be.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import wili_be.dto.TokenDto;
import wili_be.entity.Member;
import wili_be.security.JWT.JwtTokenProvider;
import wili_be.service.*;


import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static wili_be.dto.MemberDto.*;
import static wili_be.exception.CustomExceptions.*;

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
    private final JsonService jsonService;

    @PostMapping("/users/auth")
    ResponseEntity<?> validateAccessToken(HttpServletRequest httpRequest) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);
        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
        Map<String, Object> response = new HashMap<>();
        response.put("snsId", snsId);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/users/refresh-token")
    ResponseEntity<String> validateRefreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "refreshToken 없습니다.");
        }
        String snsId = redisService.getValues(refreshToken);
        TokenDto newToken = tokenService.createTokensFromRefreshToken(snsId, refreshToken);
        ResponseCookie responseCookie = memberService.createHttpOnlyCookie(newToken.getRefreshToken());
        String new_accessToken = newToken.getAccessToken();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .header("accessToken", new_accessToken)
                .body("create accessToken from refreshToken finish");
    }

    @PostMapping("/users/logout")
    ResponseEntity<String> logout(HttpServletRequest request) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        redisService.setAccessTokenBlackList(accessToken);
        return ResponseEntity.ok("set" + accessToken + "blackList");
    }

    @GetMapping("/users/{snsId}")
    ResponseEntity<String> getMemberInfo(HttpServletRequest httpRequest, @PathVariable String snsId) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        Member member = memberService.findMemberById(snsId).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "멤버가 존재하지 않습니다."));
        MemberResponseDto memberResponseDto = new MemberResponseDto(member);
        String memberResponseDtoJson = jsonService.changeMemberResponseDtoToJson(memberResponseDto);
        return ResponseEntity.ok().body(memberResponseDtoJson);
    }

    @GetMapping("/users/check/{username}")
    public ResponseEntity<Map<String, String>> validateUserName(@PathVariable String username) {
        if (memberService.validateExistingMember(username)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "사용가능 합니다.");
            return ResponseEntity.ok()
                    .body(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "중복");
            return ResponseEntity.ok()
                    .body(response);
        }
    }

    @PatchMapping("/users/{snsId}")
    public ResponseEntity<String> updateMember(HttpServletRequest httpRequest, @PathVariable String snsId, @RequestBody MemberUpdateRequestDto memberRequestDto) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        MemberResponseDto memberResponseDto = memberService.updateMember(snsId, memberRequestDto);
        String updateMemberJson = jsonService.changeMemberResponseDtoToJson(memberResponseDto);
        return ResponseEntity.ok().body(updateMemberJson);
    }

    @DeleteMapping("/users/{snsId}")
    public ResponseEntity<String> removeMember(HttpServletRequest httpServletRequest, @PathVariable String snsId) {
        String accessToken = jwtTokenProvider.resolveToken(httpServletRequest);

        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);

        List<String> imageKeys = productService.getImagesKeysByMember(snsId);
        List<String> thumbnailImageKeys = productService.getThumbnailImagesKeysByMember(snsId);
        amazonS3Service.deleteImagesByKeys(imageKeys);
        amazonS3Service.deleteImagesByKeys(thumbnailImageKeys);
        redisService.setAccessTokenBlackList(accessToken);
        memberService.removeMember(snsId);
        return ResponseEntity.ok().body(snsId + "님이 탈퇴하셨습니다.");

    }
}
