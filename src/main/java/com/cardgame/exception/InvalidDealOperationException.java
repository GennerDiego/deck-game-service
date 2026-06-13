package com.cardgame.exception;

public class InvalidDealOperationException extends RuntimeException {
  public InvalidDealOperationException(String message) {
    super(message);
  }

  public static InvalidDealOperationException emptyDeck(String gameId) {
    return new InvalidDealOperationException(
        "Cannot deal cards from empty deck in game '" + gameId + "'");
  }

  public static InvalidDealOperationException invalidCount(int count) {
    return new InvalidDealOperationException(
        "Invalid card count: " + count + ". Must be positive.");
  }
}
