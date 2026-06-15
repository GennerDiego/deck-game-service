package com.cardgame.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_NULL)
@JsonPropertyOrder({
  "id",
  "totalCardsInDeck",
  "cardsRemaining",
  "discardedCards",
  "totalDecksAdded",
  "createdAt",
  "deck",
  "players"
})
public class Game implements Serializable {

  @JsonProperty("id")
  private String id;

  @JsonProperty("deck")
  @Builder.Default
  private List<Card> gameDeck = new ArrayList<>();

  @JsonProperty("players")
  @Builder.Default
  private List<Player> players = new ArrayList<>();

  //  @JsonProperty("discardedCards")
  //  @Builder.Default
  //  private List<Card> discardedCards = new ArrayList<>();

  @JsonProperty("totalDecksAdded")
  @Builder.Default
  private int totalDecksAdded = 0;

  @JsonProperty("deckIdsInUse")
  @Builder.Default
  private Set<String> deckIdsInUse = new HashSet<>();

  @Builder.Default private Instant createdAt = Instant.now();

  public static Game createNew() {
    return Game.builder().id(UUID.randomUUID().toString()).build();
  }

  public void addDeck(Deck deck) {
    this.gameDeck.addAll(deck.getCards());
    this.deckIdsInUse.add(deck.getId());
    this.totalDecksAdded++;
  }

  public void addPlayer(Player player) {
    this.players.add(player);
  }

  public void removePlayer(String playerId) {
    Player player = getPlayer(playerId);
    if (player != null) {
      // Move player's cards to discarded pile
      //      this.discardedCards.addAll(player.getCards());
      this.players.remove(player);
    }
  }

  public boolean hasPlayer(String playerId) {
    return this.players.stream().anyMatch(p -> p.getId().equals(playerId));
  }

  public boolean hasPlayerByName(String playerName) {
    return this.players.stream().anyMatch(p -> p.getName().equalsIgnoreCase(playerName));
  }

  public Player getPlayer(String playerId) {
    return this.players.stream().filter(p -> p.getId().equals(playerId)).findFirst().orElse(null);
  }

  // Computed fields for JSON output
  @JsonProperty("totalCardsInDeck")
  public int getTotalCardsInDeck() {
    return totalDecksAdded * 52;
  }

  @JsonProperty("cardsRemaining")
  public int getCardsRemaining() {
    return gameDeck.size();
  }
}
