package com.cardgame.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_NULL)
@JsonPropertyOrder({"id", "cards", "createdAt"})
public class Deck implements Serializable {

  private static final int STANDARD_DECK_SIZE = 52;

  private String id;

  @Builder.Default private List<Card> cards = new ArrayList<>();

  @Builder.Default private Instant createdAt = Instant.now();

  private static List<Card> createStandardDeckCards() {
    List<Card> standardDeckCards = new ArrayList<>(STANDARD_DECK_SIZE);
    for (Suit suit : Suit.values()) {
      for (Rank rank : Rank.values()) {
        standardDeckCards.add(Card.builder().suit(suit).rank(rank).build());
      }
    }
    return standardDeckCards;
  }

  public static Deck createNew() {
    return Deck.builder().id(UUID.randomUUID().toString()).cards(createStandardDeckCards()).build();
  }
}
