package org.runnect.server.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.runnect.server.auth.dto.response.SocialInfoResponseDto;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleSignInService {

    @Value("${google.client-id-1}")
    private String CLIENT_ID_1;

    @Value("${google.client-id-2}")
    private String CLIENT_ID_2;

    public SocialInfoResponseDto getSocialInfo(String token) {
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2))
            .build();

        String userId = null;
        String email = null;
        try {
            GoogleIdToken idToken = verifier.verify(token);

            GoogleIdToken.Payload payload = idToken.getPayload();
            userId = payload.getSubject();
            email = payload.getEmail();
        } catch (Exception e) {
            throw new UnauthorizedException(ErrorStatus.INVALID_GOOGLE_ID_TOKEN_EXCEPTION,
                ErrorStatus.INVALID_GOOGLE_ID_TOKEN_EXCEPTION.getMessage());
        }
        return SocialInfoResponseDto.of(email, userId);
    }
}
