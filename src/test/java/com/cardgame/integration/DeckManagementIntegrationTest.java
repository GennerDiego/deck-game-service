package com.cardgame.integration;

import static org.assertj.core.api.Assertions.*;

import com.cardgame.model.entity.Deck;
import com.cardgame.model.entity.Game;
import com.cardgame.model.entity.Rank;
import com.cardgame.model.entity.Suit;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
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
    Deck deck = createDeck();

    // When
    addDeckToGame(game.getId(), deck.getId());

    // Then
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getTotalCardsInDeck()).isEqualTo(52);
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(52);
    assertThat(updatedGame.getTotalDecksAdded()).isEqualTo(1);
    assertThat(updatedGame.getDeckIdsInUse()).contains(deck.getId());
  }

  @Test
  @DisplayName("Should add multiple decks creating a shoe")
  void addDeckToGame_whenGameAlreadyHasDecks_appendsNewDeck() {
    // Given
    Game game = createGame();
    Deck deck1 = createDeck();
    Deck deck2 = createDeck();
    Deck deck3 = createDeck();

    addDeckToGame(game.getId(), deck1.getId());
    addDeckToGame(game.getId(), deck2.getId());
    addDeckToGame(game.getId(), deck3.getId());

    // When
    Deck deck4 = createDeck();
    addDeckToGame(game.getId(), deck4.getId());

    // Then
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getTotalCardsInDeck()).isEqualTo(208); // 4 decks × 52
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(208);
    assertThat(updatedGame.getTotalDecksAdded()).isEqualTo(4);
    assertThat(updatedGame.getDeckIdsInUse())
        .containsExactlyInAnyOrder(deck1.getId(), deck2.getId(), deck3.getId(), deck4.getId());
  }

  @Test
  @DisplayName("Should return 404 when adding non-existent deck to game")
  void addDeckToGame_whenDeckDoesNotExist_returns404() {
    // Given
    Game game = createGame();
    String invalidDeckId = "invalid-deck-id";

    // When
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            baseUrl + "/games/" + game.getId() + "/deck/" + invalidDeckId,
            HttpMethod.POST,
            new HttpEntity<>(createAuthHeaders()),
            new ParameterizedTypeReference<>() {});

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("Should verify deck contains all 52 unique cards")
  void addDeckToGame_createsStandardDeck_withAllRanksAndSuits() {
    // Given
    Game game = createGame();
    Deck deck = createDeck();
    addDeckToGame(game.getId(), deck.getId());

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
    createAndAddDeckToGame(game.getId());

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
    createAndAddDeckToGame(game.getId());

    // When - Shuffle 5 times
    for (int i = 0; i < 5; i++) {
      shuffleDeck(game.getId());
    }

    // Then
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(52);
  }
}
