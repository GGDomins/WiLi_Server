package wili_be.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import wili_be.controller.status.ApiResponse;
import wili_be.controller.status.UserInfoApiResponse;
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

    //accessToken을 이용해서 로그인 여부를 판단함.
    @PostMapping("/users/auth")
    ResponseEntity<ApiResponse> validateAccessToken(HttpServletRequest httpRequest) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);
        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.success_user_auth(snsId);
        return ResponseEntity.ok().body(apiResponse);
    }

    @PostMapping("/users/normal-signup")
    ResponseEntity<?> userFormLogin(@RequestBody MemberSignupDto memberSignupDto) {
        memberService.chechMemberIdExist(memberSignupDto);
        memberService.saveMember(memberSignupDto);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.success_user_signupForm();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @PostMapping("/users/normal-login")
    ResponseEntity<?> userNormalLogin(@RequestBody MemberLoginDto memberLoginDto) {
        Member member = memberService.loginMember(memberLoginDto);
        TokenDto tokenDto = tokenService.createTokens(member.getSnsId());
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();

        UserInfoApiResponse apiResponse = new UserInfoApiResponse();
        apiResponse.success_user_getInfo(memberLoginDto);

        ResponseCookie responseCookie = memberService.createHttpOnlyCookie(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .header("accessToken", accessToken)
                .body(apiResponse);
    }
    //refreshToken을 이용해서 accessToken 재발급
    @PostMapping("/users/refresh-token")
    ResponseEntity<ApiResponse> validateRefreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken.isEmpty()) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.fail_user_refresh_Token();
            throw new CustomException(HttpStatus.BAD_REQUEST, apiResponse);
        }
        String snsId = redisService.getValues(refreshToken);
        TokenDto newToken = tokenService.createTokensFromRefreshToken(snsId, refreshToken);
        ResponseCookie responseCookie = memberService.createHttpOnlyCookie(newToken.getRefreshToken());
        String new_accessToken = newToken.getAccessToken();

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.success_user_refresh_Token(snsId);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .header("accessToken", new_accessToken)
                .body(apiResponse);
    }

    //accessToken을 블랙리스트 처리한 후, 로그아웃
    @PostMapping("/users/logout")
    ResponseEntity<ApiResponse> logout(HttpServletRequest request) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        redisService.setAccessTokenBlackList(accessToken);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.success_user_logout();
        return ResponseEntity.ok(apiResponse);
    }

    //user 정보 조회
    @GetMapping("/users/{snsId}")
    ResponseEntity<UserInfoApiResponse> getMemberInfo(HttpServletRequest httpRequest, @PathVariable String snsId) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        Member member = memberService.findMemberById(snsId).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "멤버가 존재하지 않습니다."));
        MemberResponseDto memberResponseDto = new MemberResponseDto(member);
        UserInfoApiResponse apiResponse = new UserInfoApiResponse();
        apiResponse.success_user_getInfo(memberResponseDto);

        return ResponseEntity.ok().body(apiResponse);
    }

    //추가 회원가입을 할 때 닉네임 중복 여부 검사
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

    //user 정보 수정
    @PatchMapping("/users/{snsId}")
    public ResponseEntity<ApiResponse> updateMember(HttpServletRequest httpRequest, @PathVariable String snsId, @RequestBody MemberUpdateRequestDto memberRequestDto) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        MemberResponseDto memberResponseDto = memberService.updateMember(snsId, memberRequestDto);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.success_user_editInfo(memberResponseDto);

        return ResponseEntity.ok().body(apiResponse);
    }

    //user 탈퇴
    @DeleteMapping("/users/{snsId}")
    public ResponseEntity<ApiResponse> removeMember(HttpServletRequest httpServletRequest, @PathVariable String snsId) {
        String accessToken = jwtTokenProvider.resolveToken(httpServletRequest);

        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        try {
            redisService.setAccessTokenBlackList(accessToken);
            memberService.removeMember(snsId);
            List<String> imageKeys = productService.getImagesKeysByMember(snsId);
            List<String> thumbnailImageKeys = productService.getThumbnailImagesKeysByMember(snsId);
            amazonS3Service.deleteImagesByKeys(imageKeys);
            amazonS3Service.deleteImagesByKeys(thumbnailImageKeys);

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.success_user_delete();

            return ResponseEntity.ok().body(apiResponse);
        } catch (NoSuchElementException e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.fail_user_delete();
            throw new CustomException(HttpStatus.OK, apiResponse);
        }
    }
}
