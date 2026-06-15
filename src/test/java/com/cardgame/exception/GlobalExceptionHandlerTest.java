package com.cardgame.exception;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler - Unit Tests")
class GlobalExceptionHandlerTest {

  @InjectMocks private GlobalExceptionHandler exceptionHandler;

  @Mock private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    when(request.getRequestURI()).thenReturn("/api/v1/test");
  }

  @Nested
  @DisplayName("handleGameNotFound()")
  class HandleGameNotFoundTests {

    @Test
    @DisplayName("Should return 404 with error details")
    void shouldReturn404WithErrorDetails() {
      GameNotFoundException exception = new GameNotFoundException("game-123");

      ResponseEntity<ErrorResponse> response =
          exceptionHandler.handleGameNotFound(exception, request);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getStatus()).isEqualTo(404);
      assertThat(response.getBody().getError()).isEqualTo("Not Found");
      assertThat(response.getBody().getMessage()).contains("game-123");
      assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
    }
  }

  @Nested
  @DisplayName("handlePlayerNotFound()")
  class HandlePlayerNotFoundTests {

    @Test
    @DisplayName("Should return 404 with player not found message")
    void shouldReturn404WithPlayerNotFoundMessage() {
      PlayerNotFoundException exception = new PlayerNotFoundException("player-456");

      ResponseEntity<ErrorResponse> response =
          exceptionHandler.handlePlayerNotFound(exception, request);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getMessage()).contains("player-456");
    }
  }

  @Nested
  @DisplayName("handleDeckNotFound()")
  class HandleDeckNotFoundTests {

    @Test
    @DisplayName("Should return 404 with deck not found message")
    void shouldReturn404WithDeckNotFoundMessage() {
      DeckNotFoundException exception = new DeckNotFoundException("deck-789");

      ResponseEntity<ErrorResponse> response =
          exceptionHandler.handleDeckNotFound(exception, request);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getMessage()).contains("deck-789");
    }
  }

  @Nested
  @DisplayName("handleDuplicatePlayer()")
  class HandleDuplicatePlayerTests {

    @Test
    @DisplayName("Should return 409 conflict")
    void shouldReturn409Conflict() {
      DuplicatePlayerException exception = new DuplicatePlayerException("player-123", "game-456");

      ResponseEntity<ErrorResponse> response =
          exceptionHandler.handleDuplicatePlayer(exception, request);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getStatus()).isEqualTo(409);
      assertThat(response.getBody().getError()).isEqualTo("Conflict");
      assertThat(response.getBody().getMessage()).contains("player-123");
      assertThat(response.getBody().getMessage()).contains("game-456");
    }
  }

  @Nested
  @DisplayName("handleInvalidDealOperation()")
  class HandleInvalidDealOperationTests {

    @Test
    @DisplayName("Should return 400 bad request")
    void shouldReturn400BadRequest() {
      InvalidDealOperationException exception =
          new InvalidDealOperationException("Cannot deal negative cards");

      ResponseEntity<ErrorResponse> response =
          exceptionHandler.handleInvalidDealOperation(exception, request);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getMessage()).contains("Cannot deal negative cards");
    }
  }

  @Nested
  @DisplayName("handleEmptyDeck()")
  class HandleEmptyDeckTests {

    @Test
    @DisplayName("Should return 400 for empty deck")
    void shouldReturn400ForEmptyDeck() {
      EmptyDeckException exception = new EmptyDeckException("game-789");

      ResponseEntity<ErrorResponse> response = exceptionHandler.handleEmptyDeck(exception, request);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getMessage()).contains("game-789");
    }
  }

  @Nested
  @DisplayName("handleDeckAlreadyAdded()")
  class HandleDeckAlreadyAddedTests {

    @Test
    @DisplayName("Should return 409 conflict when deck already added")
    void shouldReturn409ConflictWhenDeckAlreadyAdded() {
      DeckAlreadyAddedException exception = new DeckAlreadyAddedException("deck-123", "game-456");

      ResponseEntity<ErrorResponse> response =
          exceptionHandler.handleDeckAlreadyAdded(exception, request);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getStatus()).isEqualTo(409);
      assertThat(response.getBody().getError()).isEqualTo("Conflict");
      assertThat(response.getBody().getMessage()).contains("deck-123");
      assertThat(response.getBody().getMessage()).contains("game-456");
    }
  }

  @Nested
  @DisplayName("handleDeckInUse()")
  class HandleDeckInUseTests {

    @Test
    @DisplayName("Should return 409 conflict when deck is in use")
    void shouldReturn409ConflictWhenDeckInUse() {
      DeckInUseException exception = new DeckInUseException("deck-123");

      ResponseEntity<ErrorResponse> response = exceptionHandler.handleDeckInUse(exception, request);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getStatus()).isEqualTo(409);
      assertThat(response.getBody().getError()).isEqualTo("Conflict");
      assertThat(response.getBody().getMessage()).contains("deck-123");
    }
  }

  @Nested
  @DisplayName("handleUnauthorized()")
  class HandleUnauthorizedTests {

    @Test
    @DisplayName("Should return 401 unauthorized")
    void shouldReturn401Unauthorized() {
      UnauthorizedException exception = new UnauthorizedException("Invalid API key");

      ResponseEntity<ErrorResponse> response =
          exceptionHandler.handleUnauthorized(exception, request);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getStatus()).isEqualTo(401);
      assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
      assertThat(response.getBody().getMessage()).contains("Invalid API key");
    }
  }

  @Nested
  @DisplayName("handleValidationException()")
  class HandleValidationExceptionTests {

    @Test
    @DisplayName("Should return 400 with field validation errors")
    void shouldReturn400WithFieldValidationErrors() {
      MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
      BindingResult bindingResult = mock(BindingResult.class);

      FieldError fieldError1 = new FieldError("object", "name", "must not be blank");
      FieldError fieldError2 = new FieldError("object", "age", "must be positive");

      when(exception.getBindingResult()).thenReturn(bindingResult);
      when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError1, fieldError2));

      ResponseEntity<ErrorResponse> response =
          exceptionHandler.handleValidationException(exception, request);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getMessage()).contains("name: must not be blank");
      assertThat(response.getBody().getMessage()).contains("age: must be positive");
    }

    @Test
    @DisplayName("Should return default message when no field errors")
    void shouldReturnDefaultMessageWhenNoFieldErrors() {
      MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
      BindingResult bindingResult = mock(BindingResult.class);

      when(exception.getBindingResult()).thenReturn(bindingResult);
      when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of());

      ResponseEntity<ErrorResponse> response =
          exceptionHandler.handleValidationException(exception, request);

      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
    }
  }

  @Nested
  @DisplayName("handleConstraintViolation()")
  class HandleConstraintViolationTests {

    @Test
    @DisplayName("Should return 400 with constraint violation details")
    void shouldReturn400WithConstraintViolationDetails() {
      Set<ConstraintViolation<?>> violations = new HashSet<>();
      ConstraintViolation<?> violation = mock(ConstraintViolation.class);

      when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
      when(violation.getPropertyPath().toString()).thenReturn("count");
      when(violation.getMessage()).thenReturn("must be greater than 0");

      violations.add(violation);

      ConstraintViolationException exception = new ConstraintViolationException(violations);

      ResponseEntity<ErrorResponse> response =
          exceptionHandler.handleConstraintViolation(exception, request);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getMessage()).contains("count: must be greater than 0");
    }
  }

  @Nested
  @DisplayName("handleGenericException()")
  class HandleGenericExceptionTests {

    @Test
    @DisplayName("Should return 500 for unexpected exceptions")
    void shouldReturn500ForUnexpectedExceptions() {
      Exception exception = new RuntimeException("Unexpected error");

      ResponseEntity<ErrorResponse> response =
          exceptionHandler.handleGenericException(exception, request);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getStatus()).isEqualTo(500);
      assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
      assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }
  }
}
