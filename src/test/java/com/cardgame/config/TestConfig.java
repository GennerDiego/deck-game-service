package com.cardgame.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * Test configuration to customize TestRestTemplate behavior.
 *
 * <p>Prevents TestRestTemplate from throwing exceptions on 4xx/5xx errors, allowing tests to assert
 * on error responses.
 */
@TestConfiguration
public class TestConfig {

  @Bean
  @Primary
  public TestRestTemplate testRestTemplate(RestTemplateBuilder builder) {
    RestTemplateBuilder customBuilder =
        builder.errorHandler(
            new ResponseErrorHandler() {
              private final DefaultResponseErrorHandler delegate =
                  new DefaultResponseErrorHandler();

              @Override
              public boolean hasError(ClientHttpResponse response) {
                // Don't treat 4xx/5xx as errors in tests
                return false;
              }

              @Override
              public void handleError(ClientHttpResponse response) {
                // No-op - let tests handle the response
              }
            });

    return new TestRestTemplate(customBuilder);
  }
}
