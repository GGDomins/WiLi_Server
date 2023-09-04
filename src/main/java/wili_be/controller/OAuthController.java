package wili_be.controller;


import com.github.scribejava.core.model.OAuth2AccessToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.client.HttpClientErrorException;
import wili_be.dto.TokenDto;

import wili_be.entity.LoginProvider;
import wili_be.entity.Member;
import wili_be.security.oauth.KakaoLoginBO;
import wili_be.security.oauth.NaverLoginBO;
import wili_be.service.JsonService;
import wili_be.service.MemberService;
import wili_be.service.TokenService;
import wili_be.dto.MemberDto.SocialMemberInfoDto;
import wili_be.dto.MemberDto.Member_info_Dto;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

import static wili_be.dto.MemberDto.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final MemberService memberService;
    private final TokenService tokenService;
    private final NaverLoginBO naverLoginBO;
    private final KakaoLoginBO kakaoLoginBO;

    //naver Oauth 로그인
    @RequestMapping(value = "/naver/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Member_info_Dto> callback(@RequestParam String code, @RequestParam String state) throws IOException {
        OAuth2AccessToken oauthToken;
        oauthToken = naverLoginBO.getAccessToken(code, state);
        SocialMemberInfoDto userInfo = naverLoginBO.getUserProfile(oauthToken);
        Member_info_Dto memberDto = new Member_info_Dto(userInfo, LoginProvider.NAVER);

        Optional<Member> memberOptional = memberService.findMemberById(memberDto.getSnsId());
        if (memberOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header("www-authenticate")
                    .body(memberDto);
        }

        TokenDto tokenDto = tokenService.createTokens(userInfo.getId());
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();

        ResponseCookie responseCookie = memberService.createHttpOnlyCookie(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .header("accessToken", accessToken)
                .body(memberDto);
    }

    //kakao Oauth 로그인
    @RequestMapping(value = "/kakao/callback", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<Member_info_Dto> kakaoLogin(@RequestParam("code") String code) throws IOException {
        OAuth2AccessToken oauthToken;
        oauthToken = kakaoLoginBO.getAccessToken(code);
        SocialMemberInfoDto userInfo = kakaoLoginBO.getKakaoUserInfo(oauthToken);
        Member_info_Dto memberDto = new Member_info_Dto(userInfo, LoginProvider.KAKAO);

        Optional<Member> memberOptional = memberService.findMemberById(memberDto.getSnsId());
        if (memberOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header("www-authenticate")
                    .body(memberDto);
        }

        TokenDto tokenDto = tokenService.createTokens(userInfo.getId());
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();

        ResponseCookie responseCookie = memberService.createHttpOnlyCookie(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .header("accessToken", accessToken)
                .body(memberDto);
    }

    //user 추가 회원가입
    @PostMapping("/users/signup")
    ResponseEntity<String> additionalSignUp(@RequestBody AdditionalSignupInfo additionalSignupInfo) {
        memberService.saveUser(additionalSignupInfo);
        TokenDto tokenDto = tokenService.createTokens(additionalSignupInfo.getSnsId());
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        ResponseCookie responseCookie = memberService.createHttpOnlyCookie(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .header("accessToken", accessToken)
                .body("signup SuccessFul");
    }
}


