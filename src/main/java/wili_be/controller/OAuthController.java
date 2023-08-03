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
import wili_be.service.MemberService;
import wili_be.service.TokenService;
import wili_be.dto.MemberDto.SocialMemberInfoDto;
import wili_be.dto.MemberDto.Member_info_Dto;

import java.io.IOException;
import java.util.Optional;

import static wili_be.dto.MemberDto.*;

/**
 * Handles requests for the application home page.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final MemberService memberService;
    private final TokenService tokenService;
    private final NaverLoginBO naverLoginBO;
    private final KakaoLoginBO kakaoLoginBO;

    /**
     * NAVER
     */
    @RequestMapping(value = "/naver/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> callback(@RequestParam String code, @RequestParam String state) {
        OAuth2AccessToken oauthToken;
        try {
            oauthToken = naverLoginBO.getAccessToken(code, state);
            SocialMemberInfoDto userInfo = naverLoginBO.getUserProfile(oauthToken);

            Member_info_Dto memberDto = new Member_info_Dto(userInfo,LoginProvider.NAVER);

            String jsonMemberDto = memberService.changeMemberInfoDtoToJson(memberDto);
            Optional<Member> memberOptional = memberService.findUserBySnsId(memberDto.getSnsId());
            if (memberOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .header("www-authenticate")
                        .body(jsonMemberDto);
            }

            TokenDto tokenDto = tokenService.createTokens(userInfo.getId());
            String accessToken = tokenDto.getAccessToken();
            String refreshToken = tokenDto.getRefreshToken();

            ResponseCookie responseCookie = memberService.createHttpOnlyCookie(refreshToken);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .header("accessToken", accessToken)
                    .body("Name: " + userInfo.getNickname() + ", Email: " + userInfo.getEmail() + ", id: " + userInfo.getId() + ", AccessToken: " + tokenDto.getAccessToken() + ", RefreshToken: " + tokenDto.getRefreshToken());
        } catch (IOException e) {
            // IOException 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get user profile.");
        } catch (JSONException e) {
            // JSONException 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to parse Naver API response.");
        } catch (Exception e) {
            // 그 외 모든 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during Naver callback.");
        }
    }

    /**
     * KAKAO
     */
    @RequestMapping(value = "/kakao/callback", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<String> kakaoLogin(@RequestParam("code") String code){
        log.info(code);
        OAuth2AccessToken oauthToken;
        try {
            oauthToken = kakaoLoginBO.getAccessToken(code);
            SocialMemberInfoDto userInfo = kakaoLoginBO.getKakaoUserInfo(oauthToken);

            Member_info_Dto memberDto = new Member_info_Dto(userInfo,LoginProvider.KAKAO);

            String jsonMemberDto = memberService.changeMemberInfoDtoToJson(memberDto);
            Optional<Member> memberOptional = memberService.findUserBySnsId(memberDto.getSnsId());
            if (memberOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .header("www-authenticate")
                        .body(jsonMemberDto);
            }

            TokenDto tokenDto = tokenService.createTokens(userInfo.getId());
            String accessToken = tokenDto.getAccessToken();
            String refreshToken = tokenDto.getRefreshToken();

            ResponseCookie responseCookie = memberService.createHttpOnlyCookie(refreshToken);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .header("accessToken",accessToken)
                    .body("Name: " + userInfo.getNickname() + ", Email: " + userInfo.getEmail() + ", id: " + userInfo.getId() + ", AccessToken: " + tokenDto.getAccessToken() + ", RefreshToken: " + tokenDto.getRefreshToken());
        } catch (HttpClientErrorException.BadRequest ex) {
            return ResponseEntity.badRequest().body("Kakao API Bad Request: " + ex.getStatusCode() + " " + ex.getStatusText());
        } catch (Exception ex) {
            // 그 외 모든 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during Kakao login: " + ex.getMessage());
        }
    }

    @PostMapping("/users/signup")
    ResponseEntity<String> additionalSignUp(@RequestBody AdditionalSignupInfo additionalSignupInfo) {
        try {
            memberService.saveUser(additionalSignupInfo);
            TokenDto tokenDto = tokenService.createTokens(additionalSignupInfo.getSnsId());
            String accessToken = tokenDto.getAccessToken();
            String refreshToken = tokenDto.getRefreshToken();
            ResponseCookie responseCookie = memberService.createHttpOnlyCookie(refreshToken);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .header("accessToken",accessToken)
                    .body("signup SuccessFul");

        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("additionalSignupInfo is null");
        }
    }
}


