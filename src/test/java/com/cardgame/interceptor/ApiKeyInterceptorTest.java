package com.cardgame.interceptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cardgame.annotation.AuthApiKey;
import com.cardgame.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiKeyInterceptor - Unit Tests")
class ApiKeyInterceptorTest {

  private ApiKeyInterceptor interceptor;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private HandlerMethod handlerMethod;

  @Mock private AuthApiKey authApiKeyAnnotation;

  private static final String VALID_API_KEY = "valid-test-api-key";
  private static final String INVALID_API_KEY = "invalid-api-key";
  private static final String API_KEY_HEADER = "X-API-Key";

  @BeforeEach
  void setUp() {
    interceptor = new ApiKeyInterceptor();
    ReflectionTestUtils.setField(interceptor, "validApiKey", VALID_API_KEY);
  }

  @Nested
  @DisplayName("preHandle() - With @AuthApiKey annotation")
  class WithAuthApiKeyAnnotation {

    @BeforeEach
    void setUp() {
      when(handlerMethod.getMethodAnnotation(AuthApiKey.class)).thenReturn(authApiKeyAnnotation);
    }

    @Test
    @DisplayName("Should return true when API key is valid")
    void preHandle_withValidApiKey_returnsTrue() {
      // Given
      when(request.getHeader(API_KEY_HEADER)).thenReturn(VALID_API_KEY);
      when(request.getRequestURI()).thenReturn("/api/v1/games");

      // When
      boolean result = interceptor.preHandle(request, response, handlerMethod);

      // Then
      assertThat(result).isTrue();
      verify(request, times(1)).getHeader(API_KEY_HEADER);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when API key is missing")
    void preHandle_withMissingApiKey_throwsException() {
      // Given
      when(request.getHeader(API_KEY_HEADER)).thenReturn(null);
      when(request.getRequestURI()).thenReturn("/api/v1/games");

      // When/Then
      assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("API Key is required. Provide X-API-Key header.");

      verify(request, times(1)).getHeader(API_KEY_HEADER);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when API key is blank")
    void preHandle_withBlankApiKey_throwsException() {
      // Given
      when(request.getHeader(API_KEY_HEADER)).thenReturn("   ");
      when(request.getRequestURI()).thenReturn("/api/v1/games");

      // When/Then
      assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("API Key is required. Provide X-API-Key header.");
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when API key is empty string")
    void preHandle_withEmptyApiKey_throwsException() {
      // Given
      when(request.getHeader(API_KEY_HEADER)).thenReturn("");
      when(request.getRequestURI()).thenReturn("/api/v1/games");

      // When/Then
      assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("API Key is required. Provide X-API-Key header.");
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when API key is invalid")
    void preHandle_withInvalidApiKey_throwsException() {
      // Given
      when(request.getHeader(API_KEY_HEADER)).thenReturn(INVALID_API_KEY);
      when(request.getRequestURI()).thenReturn("/api/v1/games");
      when(request.getRemoteAddr()).thenReturn("192.168.1.100");

      // When/Then
      assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("Invalid API Key.");

      verify(request, times(1)).getHeader(API_KEY_HEADER);
      verify(request, times(1)).getRemoteAddr();
    }

    @Test
    @DisplayName("Should be case-sensitive when validating API key")
    void preHandle_apiKeyIsCaseSensitive_throwsException() {
      // Given
      String wrongCaseKey = VALID_API_KEY.toUpperCase();
      when(request.getHeader(API_KEY_HEADER)).thenReturn(wrongCaseKey);
      when(request.getRequestURI()).thenReturn("/api/v1/games");

      // When/Then
      assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("Invalid API Key.");
    }

    @Test
    @DisplayName("Should trim API key before validation")
    void preHandle_withApiKeyWithWhitespace_throwsException() {
      // Given - API key with leading/trailing spaces
      when(request.getHeader(API_KEY_HEADER)).thenReturn("  " + VALID_API_KEY + "  ");
      when(request.getRequestURI()).thenReturn("/api/v1/games");

      // When/Then - Should fail because we don't trim in the interceptor
      assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("Invalid API Key.");
    }
  }

  @Nested
  @DisplayName("preHandle() - Without @AuthApiKey annotation")
  class WithoutAuthApiKeyAnnotation {

    @BeforeEach
    void setUp() {
      when(handlerMethod.getMethodAnnotation(AuthApiKey.class)).thenReturn(null);
    }

    @Test
    @DisplayName("Should return true when no @AuthApiKey annotation present")
    void preHandle_withoutAnnotation_returnsTrue() {
      // Given - No annotation, so no API key required

      // When
      boolean result = interceptor.preHandle(request, response, handlerMethod);

      // Then
      assertThat(result).isTrue();
      verify(request, never()).getHeader(anyString());
    }

    @Test
    @DisplayName("Should not check API key when endpoint is not protected")
    void preHandle_withoutAnnotation_doesNotCheckApiKey() {
      // Given - No stubbing needed, annotation returns null

      // When
      boolean result = interceptor.preHandle(request, response, handlerMethod);

      // Then
      assertThat(result).isTrue();
      verify(request, never()).getHeader(API_KEY_HEADER);
    }
  }

  @Nested
  @DisplayName("preHandle() - Non-HandlerMethod handlers")
  class NonHandlerMethodHandlers {

    @Test
    @DisplayName("Should return true when handler is not a HandlerMethod")
    void preHandle_withNonHandlerMethod_returnsTrue() {
      // Given - Handler is not a HandlerMethod (e.g., static resource)
      Object nonHandlerMethod = new Object();

      // When
      boolean result = interceptor.preHandle(request, response, nonHandlerMethod);

      // Then
      assertThat(result).isTrue();
      verify(request, never()).getHeader(anyString());
    }

    @Test
    @DisplayName("Should not validate API key for static resources")
    void preHandle_forStaticResource_returnsTrue() {
      // Given
      String staticResourceHandler = "/static/css/styles.css";

      // When
      boolean result = interceptor.preHandle(request, response, staticResourceHandler);

      // Then
      assertThat(result).isTrue();
    }
  }

  @Nested
  @DisplayName("Security - Edge Cases")
  class SecurityEdgeCases {

    @BeforeEach
    void setUp() {
      when(handlerMethod.getMethodAnnotation(AuthApiKey.class)).thenReturn(authApiKeyAnnotation);
    }

    @Test
    @DisplayName("Should reject SQL injection attempt in API key")
    void preHandle_withSqlInjectionAttempt_throwsException() {
      // Given
      String sqlInjection = "' OR '1'='1";
      when(request.getHeader(API_KEY_HEADER)).thenReturn(sqlInjection);
      when(request.getRequestURI()).thenReturn("/api/v1/games");

      // When/Then
      assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("Invalid API Key.");
    }

    @Test
    @DisplayName("Should reject script injection attempt in API key")
    void preHandle_withScriptInjectionAttempt_throwsException() {
      // Given
      String scriptInjection = "<script>alert('xss')</script>";
      when(request.getHeader(API_KEY_HEADER)).thenReturn(scriptInjection);
      when(request.getRequestURI()).thenReturn("/api/v1/games");

      // When/Then
      assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("Invalid API Key.");
    }

    @Test
    @DisplayName("Should handle very long API key gracefully")
    void preHandle_withVeryLongApiKey_throwsException() {
      // Given
      String veryLongKey = "a".repeat(10000);
      when(request.getHeader(API_KEY_HEADER)).thenReturn(veryLongKey);
      when(request.getRequestURI()).thenReturn("/api/v1/games");

      // When/Then
      assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("Invalid API Key.");
    }

    @Test
    @DisplayName("Should handle special characters in API key")
    void preHandle_withSpecialCharacters_throwsException() {
      // Given
      String specialChars = "!@#$%^&*()_+-={}[]|\\:;\"'<>?,./";
      when(request.getHeader(API_KEY_HEADER)).thenReturn(specialChars);
      when(request.getRequestURI()).thenReturn("/api/v1/games");

      // When/Then
      assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("Invalid API Key.");
    }
  }
}
