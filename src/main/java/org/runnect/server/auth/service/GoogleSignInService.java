package org.runnect.server.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.runnect.server.auth.dto.response.SocialInfoResponseDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GoogleSignInService {

    private final RestTemplate restTemplate = new RestTemplate();

    public SocialInfoResponseDto getSocialInfo(String accessToken) {
        JsonNode userResourceNode = getUserResource(accessToken);

        return SocialInfoResponseDto.of(
            userResourceNode.get("email").asText(),
            userResourceNode.get("id").asText()
        );
    }

    private JsonNode getUserResource(String accessToken) {
        String resourceUri = "https://www.googleapis.com/oauth2/v2/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity entity = new HttpEntity(headers);
        return restTemplate.exchange(resourceUri, HttpMethod.GET, entity, JsonNode.class).getBody();
    }
}
