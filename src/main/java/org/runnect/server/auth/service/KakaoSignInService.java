package org.runnect.server.auth.service;

import com.google.gson.JsonElement;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jose.shaded.json.parser.JSONParser;
import com.nimbusds.jose.shaded.json.parser.ParseException;
import lombok.RequiredArgsConstructor;
import org.runnect.server.auth.dto.response.SocialInfoResponseDto;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.UnauthorizedException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
public class KakaoSignInService {

    public SocialInfoResponseDto getSocialInfo(String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();

        String userId = null;
        String email = null;

        try {
            // 카카오 API가 4xx/5xx를 반환하면(토큰 만료/무효 등, 흔히 발생) RestTemplate이
            // 예외를 던진다. 이 try 블록 밖에 있으면 여기서 잡히지 않고 그대로 500으로
            // 새어나가 버리므로(실제로는 401로 처리돼야 할 흔한 케이스인데도) 같이 묶는다.
            ResponseEntity<String> response = rt.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.POST,
                    kakaoUserInfoRequest,
                    String.class
            );

            // responseBody 속 정보 꺼내기
            String responseBody = response.getBody();

            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(responseBody);
            JSONObject kakao_account = (JSONObject) obj.get("kakao_account");


            userId = obj.get("id").toString();
            email = kakao_account.get("email").toString();

        } catch (Exception e) {
            throw new UnauthorizedException(ErrorStatus.INVALID_KAKAO_ID_TOKEN_EXCEPTION,
                    ErrorStatus.INVALID_KAKAO_ID_TOKEN_EXCEPTION.getMessage());
        }
        return SocialInfoResponseDto.of(email, userId);
    }
}
