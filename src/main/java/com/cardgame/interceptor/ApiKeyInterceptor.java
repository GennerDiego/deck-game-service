package com.cardgame.interceptor;

import com.cardgame.annotation.AuthApiKey;
import com.cardgame.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that validates API key authentication for endpoints annotated with {@link
 * AuthApiKey}.
 *
 * <p>Checks for the presence and validity of the X-API-Key header before allowing the request to
 * proceed.
 */
@Slf4j
@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

  private static final String API_KEY_HEADER = "X-API-Key";

  @Value("${app.security.api-key}")
  private String validApiKey;

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {

    // Check if handler is a method with @AuthApiKey annotation
    if (handler instanceof HandlerMethod handlerMethod) {
      AuthApiKey authApiKey = handlerMethod.getMethodAnnotation(AuthApiKey.class);

      if (authApiKey != null) {
        String apiKey = request.getHeader(API_KEY_HEADER);

        log.debug("API Key validation for endpoint: {}", request.getRequestURI());

        if (apiKey == null || apiKey.isBlank()) {
          log.warn("Missing API Key for endpoint: {}", request.getRequestURI());
          throw new UnauthorizedException("API Key is required. Provide X-API-Key header.");
        }

        if (!validApiKey.equals(apiKey)) {
          log.warn(
              "Invalid API Key attempt for endpoint: {} from IP: {}",
              request.getRequestURI(),
              request.getRemoteAddr());
          throw new UnauthorizedException("Invalid API Key.");
        }

        log.debug("API Key validated successfully for endpoint: {}", request.getRequestURI());
      }
    }

    return true;
  }
}
