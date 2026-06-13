package com.cardgame.model.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deck implements Serializable {

  private static final int STANDARD_DECK_SIZE = 52;

  private String id;
  private List<Card> cards;
  private Instant createdAt;

  public Deck(String id) {
    this.id = id;
    this.cards = createStandardDeckCards();
    this.createdAt = Instant.now();
  }

  private List<Card> createStandardDeckCards() {
    List<Card> standardDeckCards = new ArrayList<>(STANDARD_DECK_SIZE);
    for (Suit suit : Suit.values()) {
      for (Rank rank : Rank.values()) {
        standardDeckCards.add(new Card(suit, rank));
      }
    }
    return standardDeckCards;
  }

  public static Deck createNew() {
    return new Deck(UUID.randomUUID().toString());
  }
}
