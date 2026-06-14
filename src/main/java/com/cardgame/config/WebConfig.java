package com.cardgame.config;

import com.cardgame.interceptor.ApiKeyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for registering interceptors.
 *
 * <p>Registers the API key interceptor to validate authentication on protected endpoints.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final ApiKeyInterceptor apiKeyInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(apiKeyInterceptor).addPathPatterns("/**");
  }
}
