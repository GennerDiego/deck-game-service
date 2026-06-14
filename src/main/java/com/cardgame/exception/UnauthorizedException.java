package com.cardgame.exception;

/**
 * Exception thrown when API key authentication fails.
 *
 * <p>Results in HTTP 401 Unauthorized response.
 */
public class UnauthorizedException extends RuntimeException {

  public UnauthorizedException(String message) {
    super(message);
  }
}
