package com.cardgame.integration;

import static org.assertj.core.api.Assertions.*;

import com.cardgame.model.dto.PlayerScoreResponse;
import com.cardgame.model.entity.Card;
import com.cardgame.model.entity.Game;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("Query Operations - Scores, Suit Counts, Card Counts")
class QueryOperationsIntegrationTest extends BaseIntegrationTest {

  @Nested
  @DisplayName("Player Scores - Core Requirement")
  class PlayerScoresTests {

    @Test
    @DisplayName("[CORE] Should return players sorted by score descending")
    void getPlayerScores_withMultiplePlayers_returnsSortedByTotalValueDescending() {
      // Given
      Game game = createGame();
      addDeckToGame(game.getId());
      addDeckToGame(game.getId());

      String playerA = addPlayer(game.getId(), "Alice");
      String playerB = addPlayer(game.getId(), "Bob");
      String playerC = addPlayer(game.getId(), "Charlie");

      // Deal cards
      dealCards(game.getId(), playerA, 2);
      dealCards(game.getId(), playerB, 2);
      dealCards(game.getId(), playerC, 2);

      // When
      List<PlayerScoreResponse> scores = getPlayerScores(game.getId());

      // Then
      assertThat(scores).hasSize(3);

      // Verify descending order
      for (int i = 0; i < scores.size() - 1; i++) {
        assertThat(scores.get(i).getTotalValue())
            .isGreaterThanOrEqualTo(scores.get(i + 1).getTotalValue());
      }

      // Verify all players present
      List<String> playerNames =
          scores.stream().map(PlayerScoreResponse::getName).collect(Collectors.toList());
      assertThat(playerNames).containsExactlyInAnyOrder("Alice", "Bob", "Charlie");
    }

    @Test
    @DisplayName("Should return empty list when game has no players")
    void getPlayerScores_whenGameHasNoPlayers_returnsEmptyList() {
      // Given
      Game game = createGame();

      // When
      List<PlayerScoreResponse> scores = getPlayerScores(game.getId());

      // Then
      assertThat(scores).isEmpty();
    }

    @Test
    @DisplayName("[CORE] Should calculate scores using correct face values")
    void getPlayerScores_usesCorrectFaceValues() {
      // Given
      Game game = createGame();
      addDeckToGame(game.getId());
      addDeckToGame(game.getId());
      addDeckToGame(game.getId());
      addDeckToGame(game.getId());

      String playerId = addPlayer(game.getId(), "TestPlayer");
      dealCards(game.getId(), playerId, 20);

      // When
      List<PlayerScoreResponse> scores = getPlayerScores(game.getId());

      // Then
      assertThat(scores).hasSize(1);
      PlayerScoreResponse score = scores.get(0);

      // Verify face value calculation
      List<Card> playerCards = getPlayerCards(game.getId(), playerId);
      int expectedTotal = playerCards.stream().mapToInt(Card::getFaceValue).sum();

      assertThat(score.getTotalValue()).isEqualTo(expectedTotal);
      assertThat(score.getCardCount()).isEqualTo(20);
    }
  }

  @Nested
  @DisplayName("Suit Counts Query")
  class SuitCountsTests {

    @Test
    @DisplayName("Should return 13 per suit for full deck")
    void getSuitCounts_withFullDeck_returns13PerSuit() {
      // Given
      Game game = createGame();
      addDeckToGame(game.getId());

      // When
      Map<String, Integer> suitCounts = getSuitCounts(game.getId());

      // Then
      assertThat(suitCounts).hasSize(4);
      assertThat(suitCounts.get("HEARTS")).isEqualTo(13);
      assertThat(suitCounts.get("SPADES")).isEqualTo(13);
      assertThat(suitCounts.get("CLUBS")).isEqualTo(13);
      assertThat(suitCounts.get("DIAMONDS")).isEqualTo(13);
    }

    @Test
    @DisplayName("Should return correct counts after dealing cards")
    void getSuitCounts_afterDealingCards_returnsRemainingCounts() {
      // Given
      Game game = createGame();
      addDeckToGame(game.getId());
      String playerId = addPlayer(game.getId(), "Alice");

      dealCards(game.getId(), playerId, 10);

      // When
      Map<String, Integer> suitCounts = getSuitCounts(game.getId());

      // Then
      assertThat(suitCounts).hasSize(4);

      int totalRemaining = suitCounts.values().stream().mapToInt(Integer::intValue).sum();
      assertThat(totalRemaining).isEqualTo(42);
    }

    @Test
    @DisplayName("Should return all zeros when deck is empty")
    void getSuitCounts_whenDeckIsEmpty_returnsAllZeros() {
      // Given
      Game game = createGame();
      addDeckToGame(game.getId());
      String playerId = addPlayer(game.getId(), "Alice");

      dealCards(game.getId(), playerId, 52);

      // When
      Map<String, Integer> suitCounts = getSuitCounts(game.getId());

      // Then
      assertThat(suitCounts).hasSize(4);
      assertThat(suitCounts.get("HEARTS")).isEqualTo(0);
      assertThat(suitCounts.get("SPADES")).isEqualTo(0);
      assertThat(suitCounts.get("CLUBS")).isEqualTo(0);
      assertThat(suitCounts.get("DIAMONDS")).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return combined counts for multiple decks")
    void getSuitCounts_withMultipleDecks_returnsCombinedCounts() {
      // Given
      Game game = createGame();
      addDeckToGame(game.getId());
      addDeckToGame(game.getId());

      // When
      Map<String, Integer> suitCounts = getSuitCounts(game.getId());

      // Then
      assertThat(suitCounts).hasSize(4);
      assertThat(suitCounts.get("HEARTS")).isEqualTo(26);
      assertThat(suitCounts.get("SPADES")).isEqualTo(26);
      assertThat(suitCounts.get("CLUBS")).isEqualTo(26);
      assertThat(suitCounts.get("DIAMONDS")).isEqualTo(26);
    }
  }

  @Nested
  @DisplayName("Card Counts Query - Core Requirement")
  class CardCountsTests {

    @Test
    @DisplayName("[CORE] Should return sorted card counts for full deck")
    void getCardCounts_withFullDeck_returnsSortedBySuitAndValue() {
      // Given
      Game game = createGame();
      addDeckToGame(game.getId());

      // When
      Map<String, Integer> cardCounts = getCardCounts(game.getId());

      // Then
      assertThat(cardCounts).hasSize(52);
      cardCounts.values().forEach(count -> assertThat(count).isEqualTo(1));
    }

    @Test
    @DisplayName("Should show duplicate counts for multiple decks")
    void getCardCounts_withMultipleDecks_showsDuplicateCounts() {
      // Given
      Game game = createGame();
      addDeckToGame(game.getId());
      addDeckToGame(game.getId());
      addDeckToGame(game.getId());

      // When
      Map<String, Integer> cardCounts = getCardCounts(game.getId());

      // Then
      assertThat(cardCounts).hasSize(52);
      cardCounts.values().forEach(count -> assertThat(count).isEqualTo(3));
    }

    @Test
    @DisplayName("Should reflect remaining cards after partial dealing")
    void getCardCounts_afterDealingSomeCards_reflectsRemainingCards() {
      // Given
      Game game = createGame();
      addDeckToGame(game.getId());
      addDeckToGame(game.getId());

      String playerId = addPlayer(game.getId(), "Alice");
      dealCards(game.getId(), playerId, 50);

      // When
      Map<String, Integer> cardCounts = getCardCounts(game.getId());

      // Then
      int totalRemaining = cardCounts.values().stream().mapToInt(Integer::intValue).sum();
      assertThat(totalRemaining).isEqualTo(54);
    }

    @Test
    @DisplayName("Should return empty map when deck is empty")
    void getCardCounts_whenDeckIsEmpty_returnsEmptyMap() {
      // Given
      Game game = createGame();
      addDeckToGame(game.getId());
      String playerId = addPlayer(game.getId(), "Alice");

      dealCards(game.getId(), playerId, 52);

      // When
      Map<String, Integer> cardCounts = getCardCounts(game.getId());

      // Then
      assertThat(cardCounts).isEmpty();
    }
  }

  @Nested
  @DisplayName("Player Cards Query")
  class PlayerCardsQueryTests {

    @Test
    @DisplayName("Should return all cards for player with cards")
    void getPlayerCards_whenPlayerHasCards_returnsAllCards() {
      // Given
      Game game = createGame();
      addDeckToGame(game.getId());
      String playerId = addPlayer(game.getId(), "Alice");

      List<Card> dealtCards = dealCards(game.getId(), playerId, 5);

      // When
      List<Card> playerCards = getPlayerCards(game.getId(), playerId);

      // Then
      assertThat(playerCards).hasSize(5);
      assertThat(playerCards).containsExactlyElementsOf(dealtCards);
    }

    @Test
    @DisplayName("Should return empty list for player with no cards")
    void getPlayerCards_whenPlayerHasNoCards_returnsEmptyList() {
      // Given
      Game game = createGame();
      String playerId = addPlayer(game.getId(), "Bob");

      // When
      List<Card> playerCards = getPlayerCards(game.getId(), playerId);

      // Then
      assertThat(playerCards).isEmpty();
    }

    @Test
    @DisplayName("Should return 404 for non-existent player")
    void getPlayerCards_whenPlayerDoesNotExist_returns404() {
      // Given
      Game game = createGame();
      String invalidPlayerId = "invalid-player-id";

      // When
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              baseUrl + "/games/" + game.getId() + "/players/" + invalidPlayerId + "/cards",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<>() {});

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
  }
}
