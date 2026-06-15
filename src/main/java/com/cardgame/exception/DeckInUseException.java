package com.cardgame.exception;

public class DeckInUseException extends RuntimeException {

  public DeckInUseException(String deckId) {
    super("Deck with ID '" + deckId + "' is in use by a game and cannot be deleted");
  }
}
