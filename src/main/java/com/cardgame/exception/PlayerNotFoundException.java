package com.cardgame.exception;

public class PlayerNotFoundException extends RuntimeException {
  public PlayerNotFoundException(String playerId) {
    super("Player with ID '" + playerId + "' not found");
  }

  public PlayerNotFoundException(String playerId, String gameId) {
    super("Player with ID '" + playerId + "' not found in game '" + gameId + "'");
  }
}
