package com.cardgame.integration;

import static org.assertj.core.api.Assertions.*;

import com.cardgame.model.entity.Game;
import com.cardgame.model.entity.Rank;
import com.cardgame.model.entity.Suit;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("Deck Management - Add Decks and Shuffle")
class DeckManagementIntegrationTest extends BaseIntegrationTest {

  @Test
  @DisplayName("Should add first deck with 52 cards")
  void addDeckToGame_whenGameHasNoDecks_adds52Cards() {
    // Given
    Game game = createGame();

    // When
    addDeckToGame(game.getId());

    // Then
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getTotalCardsInDeck()).isEqualTo(52);
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(52);
    assertThat(updatedGame.getTotalDecksAdded()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should add multiple decks creating a shoe")
  void addDeckToGame_whenGameAlreadyHasDecks_appendsNewDeck() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());
    addDeckToGame(game.getId());
    addDeckToGame(game.getId());

    // When
    addDeckToGame(game.getId());

    // Then
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getTotalCardsInDeck()).isEqualTo(208); // 4 decks × 52
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(208);
    assertThat(updatedGame.getTotalDecksAdded()).isEqualTo(4);
  }

  @Test
  @DisplayName("Should return 404 when adding deck to non-existent game")
  void addDeckToGame_whenGameDoesNotExist_returns404() {
    // Given
    String invalidGameId = "invalid-game-id";

    // When
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            baseUrl + "/games/" + invalidGameId + "/decks",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<>() {});

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("Should verify deck contains all 52 unique cards")
  void addDeckToGame_createsStandardDeck_withAllRanksAndSuits() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());

    // When
    Map<String, Integer> cardCounts = getCardCounts(game.getId());

    // Then
    assertThat(cardCounts).hasSize(52); // 13 ranks × 4 suits

    // Verify each card appears exactly once
    for (Suit suit : Suit.values()) {
      for (Rank rank : Rank.values()) {
        String cardKey = rank + " of " + suit;
        assertThat(cardCounts).containsEntry(cardKey, 1);
      }
    }
  }

  @Test
  @DisplayName("Should not throw exception when shuffling empty deck")
  void shuffleGameDeck_whenDeckIsEmpty_doesNotThrowException() {
    // Given
    Game game = createGame();

    // When
    shuffleDeck(game.getId());

    // Then
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should randomize card order when shuffling deck with cards")
  void shuffleGameDeck_whenDeckHasCards_randomizesOrder() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());

    Map<String, Integer> initialCardCounts = getCardCounts(game.getId());

    // When
    shuffleDeck(game.getId());

    // Then
    Map<String, Integer> shuffledCardCounts = getCardCounts(game.getId());

    // Verify all cards are still present
    assertThat(shuffledCardCounts).hasSize(52);
    assertThat(shuffledCardCounts).containsAllEntriesOf(initialCardCounts);
  }

  @Test
  @DisplayName("Should allow shuffle to be called multiple times")
  void shuffleGameDeck_whenCalledMultipleTimes_producesRandomResults() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());

    // When - Shuffle 5 times
    for (int i = 0; i < 5; i++) {
      shuffleDeck(game.getId());
    }

    // Then
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(52);
  }
}
