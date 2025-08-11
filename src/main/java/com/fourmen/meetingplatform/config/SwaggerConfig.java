package com.fourmen.meetingplatform.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "4-MEN 비즈니스 미팅 플랫폼 API", version = "v1.0.0", description = "AI 기반 비즈니스 미팅 플랫폼의 API 명세서입니다."), security = {
        @SecurityRequirement(name = "bearerAuth"),
        @SecurityRequirement(name = "csrfAuth")
})
public class SwaggerConfig {

    @SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", in = SecuritySchemeIn.HEADER, paramName = "Authorization")
    @SecurityScheme(name = "csrfAuth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = "X-XSRF-TOKEN")
    public static class MultipleSecuritySchemes {
    }
}