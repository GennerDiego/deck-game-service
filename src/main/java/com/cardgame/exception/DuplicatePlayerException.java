package com.cardgame.exception;

public class DuplicatePlayerException extends RuntimeException {
  public DuplicatePlayerException(String playerId, String gameId) {
    super("Player with ID '" + playerId + "' already exists in game '" + gameId + "'");
  }
}
