package org.runnect.server.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String ACCESS_TOKEN_HEADER = "accessToken";
    private static final String REFRESH_TOKEN_HEADER = "refreshToken";

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme accessTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(ACCESS_TOKEN_HEADER);

        SecurityScheme refreshTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(REFRESH_TOKEN_HEADER);

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(ACCESS_TOKEN_HEADER)
                .addList(REFRESH_TOKEN_HEADER);

        return new OpenAPI()
                .info(new Info()
                        .title("Runnect API")
                        .description("Runnect 서버 API 문서")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(ACCESS_TOKEN_HEADER, accessTokenScheme)
                        .addSecuritySchemes(REFRESH_TOKEN_HEADER, refreshTokenScheme))
                .addSecurityItem(securityRequirement);
    }
}
