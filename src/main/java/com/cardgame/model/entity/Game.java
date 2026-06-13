package com.cardgame.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_NULL)
@RedisHash("games")
public class Game implements Serializable {

  @Id
  @JsonProperty("gameId")
  private String id;

  @TimeToLive @JsonIgnore @Builder.Default private Long ttl = 86400L; // 24 hours in seconds

  @JsonProperty("deck")
  @Builder.Default
  private List<Card> gameDeck = new ArrayList<>();

  @JsonIgnore @Builder.Default private Map<String, Player> players = new HashMap<>();

  @JsonProperty("discardedCards")
  @Builder.Default
  private List<Card> discardedCards = new ArrayList<>();

  @JsonProperty("totalDecksAdded")
  @Builder.Default
  private int totalDecksAdded = 0;

  @Builder.Default private Instant createdAt = Instant.now();

  public static Game createNew() {
    return Game.builder().id(UUID.randomUUID().toString()).build();
  }

  public void addDeck(Deck deck) {
    this.gameDeck.addAll(deck.getCards());
    this.totalDecksAdded++;
  }

  public void addPlayer(Player player) {
    this.players.put(player.getId(), player);
  }

  public void removePlayer(String playerId) {
    Player player = this.players.get(playerId);
    if (player != null) {
      // Move cartas do player para discardedCards
      this.discardedCards.addAll(player.getCards());
    }
    this.players.remove(playerId);
  }

  public boolean hasPlayer(String playerId) {
    return this.players.containsKey(playerId);
  }

  public Player getPlayer(String playerId) {
    return this.players.get(playerId);
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

  @JsonProperty("players")
  public List<Player> getPlayersList() {
    return players.values().stream()
        .sorted((a, b) -> Integer.compare(b.getTotalValue(), a.getTotalValue()))
        .collect(Collectors.toList());
  }
}
