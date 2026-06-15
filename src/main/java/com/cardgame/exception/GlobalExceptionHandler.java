package com.cardgame.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(GameNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleGameNotFound(
      GameNotFoundException ex, HttpServletRequest request) {
    log.warn("Game not found: {}", ex.getMessage());
    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(PlayerNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePlayerNotFound(
      PlayerNotFoundException ex, HttpServletRequest request) {
    log.warn("Player not found: {}", ex.getMessage());
    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(DeckNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleDeckNotFound(
      DeckNotFoundException ex, HttpServletRequest request) {
    log.warn("Deck not found: {}", ex.getMessage());
    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(DuplicatePlayerException.class)
  public ResponseEntity<ErrorResponse> handleDuplicatePlayer(
      DuplicatePlayerException ex, HttpServletRequest request) {
    log.warn("Duplicate player: {}", ex.getMessage());
    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(InvalidDealOperationException.class)
  public ResponseEntity<ErrorResponse> handleInvalidDealOperation(
      InvalidDealOperationException ex, HttpServletRequest request) {
    log.warn("Invalid deal operation: {}", ex.getMessage());
    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(EmptyDeckException.class)
  public ResponseEntity<ErrorResponse> handleEmptyDeck(
      EmptyDeckException ex, HttpServletRequest request) {
    log.warn("Empty deck: {}", ex.getMessage());
    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(DeckInUseException.class)
  public ResponseEntity<ErrorResponse> handleDeckInUse(
      DeckInUseException ex, HttpServletRequest request) {
    log.warn("Deck in use: {}", ex.getMessage());
    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorized(
      UnauthorizedException ex, HttpServletRequest request) {
    log.warn("Unauthorized access attempt: {}", ex.getMessage());
    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            ex.getMessage(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    log.warn("Validation error: {}", ex.getMessage());
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + ", " + b)
            .orElse("Validation failed");

    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(), "Bad Request", message, request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    log.warn("Constraint violation: {}", ex.getMessage());
    String message =
        ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .reduce((a, b) -> a + ", " + b)
            .orElse("Constraint violation");

    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(), "Bad Request", message, request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex, HttpServletRequest request) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);
    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
