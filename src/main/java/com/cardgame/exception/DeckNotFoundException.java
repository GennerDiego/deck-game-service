package com.cardgame.exception;

public class DeckNotFoundException extends RuntimeException {
  public DeckNotFoundException(String deckId) {
    super("Deck with ID '" + deckId + "' not found");
  }
}
