package com.cardgame.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation.
 *
 * <p>Configures API key authentication (X-API-Key header) for Swagger UI.
 */
@Configuration
public class OpenApiConfig {

  private static final String API_KEY_SCHEME_NAME = "X-API-Key";

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Deck Game Service API")
                .version("1.0")
                .description(
                    "REST API for managing poker-style deck of cards games. "
                        + "POST and DELETE operations require X-API-Key header authentication."))
        .components(
            new Components()
                .addSecuritySchemes(
                    API_KEY_SCHEME_NAME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
                        .description(
                            "API Key for authentication (default: default-api-key-change-me)")));
    // Note: Security is applied per-endpoint using @SecurityRequirement on @AuthApiKey annotated
    // methods
    // Global security disabled to allow GET endpoints without authentication
  }
}
