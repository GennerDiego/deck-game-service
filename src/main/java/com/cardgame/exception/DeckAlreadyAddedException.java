package com.cardgame.exception;

public class DeckAlreadyAddedException extends RuntimeException {
  public DeckAlreadyAddedException(String deckId, String gameId) {
    super("Deck with ID '" + deckId + "' is already added to game '" + gameId + "'");
  }
}
