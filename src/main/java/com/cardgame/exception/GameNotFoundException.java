package com.cardgame.exception;

public class GameNotFoundException extends RuntimeException {
  public GameNotFoundException(String gameId) {
    super("Game with ID '" + gameId + "' not found");
  }
}
