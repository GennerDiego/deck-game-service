package com.cardgame.model.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game implements Serializable {

  private String id;
  private List<Card> gameDeck;
  private Map<String, Player> players;
  private Map<String, List<Card>> dealtCards;
  private Instant createdAt;

  public Game(String id) {
    this.id = id;
    this.gameDeck = new ArrayList<>();
    this.players = new HashMap<>();
    this.dealtCards = new HashMap<>();
    this.createdAt = Instant.now();
  }

  public static Game createNew() {
    return new Game(UUID.randomUUID().toString());
  }

  public void addDeck(Deck deck) {
    this.gameDeck.addAll(deck.getCards());
  }

  public void addPlayer(Player player) {
    this.players.put(player.getId(), player);
    this.dealtCards.put(player.getId(), new ArrayList<>());
  }

  public void removePlayer(String playerId) {
    this.players.remove(playerId);
    this.dealtCards.remove(playerId);
  }

  public boolean hasPlayer(String playerId) {
    return this.players.containsKey(playerId);
  }

  public int getRemainingCardsCount() {
    return this.gameDeck.size();
  }
}
