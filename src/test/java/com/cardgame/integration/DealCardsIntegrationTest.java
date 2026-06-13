package com.cardgame.integration;

import static org.assertj.core.api.Assertions.*;

import com.cardgame.model.entity.Card;
import com.cardgame.model.entity.Game;
import com.cardgame.model.entity.Rank;
import com.cardgame.model.entity.Suit;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("Deal Cards Operations - Core Requirements")
class DealCardsIntegrationTest extends BaseIntegrationTest {

  @Test
  @DisplayName("Should deal single card successfully")
  void dealCards_withCount1_dealsSingleCard() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());
    String playerId = addPlayer(game.getId(), "Alice");

    // When
    List<Card> dealtCards = dealCards(game.getId(), playerId, 1);

    // Then
    assertThat(dealtCards).hasSize(1);

    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(51);

    List<Card> playerCards = getPlayerCards(game.getId(), playerId);
    assertThat(playerCards).hasSize(1);
    assertThat(playerCards.get(0)).isEqualTo(dealtCards.get(0));
  }

  @Test
  @DisplayName("Should deal multiple cards at once")
  void dealCards_withCountN_dealsNCards() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());
    String playerId = addPlayer(game.getId(), "Bob");

    // When
    List<Card> dealtCards = dealCards(game.getId(), playerId, 5);

    // Then
    assertThat(dealtCards).hasSize(5);

    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(47);

    List<Card> playerCards = getPlayerCards(game.getId(), playerId);
    assertThat(playerCards).hasSize(5);
  }

  @Test
  @DisplayName("[CORE] Should deal all 52 cards in random order after shuffle")
  void dealCards_afterShuffle_dealsAll52CardsInRandomOrder() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());
    String playerId = addPlayer(game.getId(), "Alice");

    // When - Shuffle then deal 52 times
    shuffleDeck(game.getId());

    List<Card> allDealtCards = new ArrayList<>();
    for (int i = 0; i < 52; i++) {
      List<Card> dealtCards = dealCards(game.getId(), playerId, 1);
      assertThat(dealtCards).hasSize(1);
      allDealtCards.add(dealtCards.get(0));
    }

    // Then
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(0);

    List<Card> playerCards = getPlayerCards(game.getId(), playerId);
    assertThat(playerCards).hasSize(52);

    // Verify all 52 unique cards were dealt
    Set<String> uniqueCards =
        playerCards.stream()
            .map(card -> card.getRank() + " of " + card.getSuit())
            .collect(Collectors.toSet());
    assertThat(uniqueCards).hasSize(52);

    // Verify each rank and suit appears correct number of times
    Map<Suit, Long> suitCounts =
        playerCards.stream().collect(Collectors.groupingBy(Card::getSuit, Collectors.counting()));
    assertThat(suitCounts).hasSize(4);
    suitCounts.values().forEach(count -> assertThat(count).isEqualTo(13));

    Map<Rank, Long> rankCounts =
        playerCards.stream().collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));
    assertThat(rankCounts).hasSize(13);
    rankCounts.values().forEach(count -> assertThat(count).isEqualTo(4));
  }

  @Test
  @DisplayName("[CORE] Should return empty list on 53rd deal from empty deck")
  void dealCards_whenDeckIsEmpty_returnsNoCards() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());
    String playerId = addPlayer(game.getId(), "Alice");

    // Deal all 52 cards
    shuffleDeck(game.getId());
    for (int i = 0; i < 52; i++) {
      dealCards(game.getId(), playerId, 1);
    }

    // When - 53rd deal
    List<Card> dealtCards = dealCards(game.getId(), playerId, 1);

    // Then
    assertThat(dealtCards).isEmpty();

    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(0);

    List<Card> playerCards = getPlayerCards(game.getId(), playerId);
    assertThat(playerCards).hasSize(52); // Still has original 52 cards
  }

  @Test
  @DisplayName("Should deal only available cards when requesting more than available")
  void dealCards_whenRequestingMoreThanAvailable_dealsOnlyAvailableCards() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());
    String playerId = addPlayer(game.getId(), "Bob");

    // Deal 47 cards first, leaving 5
    dealCards(game.getId(), playerId, 47);

    // When - Request 10 cards when only 5 remain
    List<Card> dealtCards = dealCards(game.getId(), playerId, 10);

    // Then
    assertThat(dealtCards).hasSize(5);

    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(0);

    List<Card> playerCards = getPlayerCards(game.getId(), playerId);
    assertThat(playerCards).hasSize(52);
  }

  @Test
  @DisplayName("Should return 400 when dealing with invalid count")
  void dealCards_withNegativeOrZeroCount_returns400BadRequest() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());
    String playerId = addPlayer(game.getId(), "Alice");

    // When - Count = 0
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            baseUrl + "/games/" + game.getId() + "/players/" + playerId + "/deal?count=0",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<>() {});

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().get("message").toString()).contains("positive");
  }

  @Test
  @DisplayName("Should return 404 when dealing to non-existent player")
  void dealCards_whenPlayerDoesNotExist_returns404() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());
    String invalidPlayerId = "invalid-player-id";

    // When
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            baseUrl + "/games/" + game.getId() + "/players/" + invalidPlayerId + "/deal?count=1",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<>() {});

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("Should deal from multiple decks (shoe)")
  void dealCards_withMultipleDecks_dealsFromCombinedShoe() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());
    addDeckToGame(game.getId());
    addDeckToGame(game.getId()); // 3 decks = 156 cards

    String playerId = addPlayer(game.getId(), "Alice");

    // When
    List<Card> dealtCards = dealCards(game.getId(), playerId, 100);

    // Then
    assertThat(dealtCards).hasSize(100);

    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(56);

    List<Card> playerCards = getPlayerCards(game.getId(), playerId);
    assertThat(playerCards).hasSize(100);
  }

  @Test
  @DisplayName("Should shuffle only remaining cards after dealing")
  void shuffleGameDeck_afterDealingCards_shufflesOnlyRemainingCards() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());
    String playerId = addPlayer(game.getId(), "Alice");

    // Deal 10 cards
    List<Card> dealtCards = dealCards(game.getId(), playerId, 10);

    // When
    shuffleDeck(game.getId());

    // Then
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(42);

    // Verify player still has the same 10 cards
    List<Card> playerCards = getPlayerCards(game.getId(), playerId);
    assertThat(playerCards).hasSize(10);
    assertThat(playerCards).containsExactlyElementsOf(dealtCards);
  }
}
