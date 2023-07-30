package wili_be.security.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import wili_be.dto.MemberDto.SocialMemberInfoDto;

import java.io.IOException;


@Component
@Slf4j
public class KakaoLoginBO {
    @Value("${spring.kakao.redirect_uri}")
    private String REDIRECT_URI;
    @Value("${spring.kakao.client_id}")
    private String CLIENT_ID;
    @Value("${spring.kakao.profile-api-url}")
    private String PROFILE_API_URL;

    @Value("${spring.kakao.client_secret}")
    private String CLIENT_SECRET;


    public OAuth2AccessToken getAccessToken(String code) throws IOException {
        // ScribeJava의 OAuth20Service를 생성
        OAuth20Service oAuth20Service = new ServiceBuilder()
                .apiKey(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback(REDIRECT_URI)
                .build(OAuthApi.KakaoOAuthApi.instance());
        log.info(oAuth20Service.toString());

        // 전달받은 인증 코드를 사용하여 액세스 토큰을 요청
        OAuth2AccessToken accessToken = oAuth20Service.getAccessToken(code);

        return accessToken;
    }

    public SocialMemberInfoDto getKakaoUserInfo(OAuth2AccessToken accessToken) throws IOException {
        // OAuth20Service 생성
        OAuth20Service oAuth20Service = new ServiceBuilder()
                .apiKey(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .build(OAuthApi.KakaoOAuthApi.instance());

        // 전달받은 액세스 토큰을 이용하여 OAuth2AccessToken 생성


        // 프로필 API 호출
        OAuthRequest request = new OAuthRequest(Verb.POST, PROFILE_API_URL, oAuth20Service);
        oAuth20Service.signRequest(accessToken, request);
        com.github.scribejava.core.model.Response response = request.send();

        // responseBody에 있는 정보를 꺼냄
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        Long id = jsonNode.get("id").asLong();
        String email = jsonNode.get("kakao_account").get("email").asText();
        String nickname = jsonNode.get("properties").get("nickname").asText();

        return new SocialMemberInfoDto(id.toString(), nickname, email);
    }
}