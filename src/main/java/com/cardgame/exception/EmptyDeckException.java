package com.cardgame.exception;

public class EmptyDeckException extends RuntimeException {
  public EmptyDeckException(String gameId) {
    super("Game deck is empty in game '" + gameId + "'");
  }
}
