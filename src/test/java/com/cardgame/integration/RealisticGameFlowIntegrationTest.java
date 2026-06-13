package com.cardgame.integration;

import static org.assertj.core.api.Assertions.*;

import com.cardgame.model.dto.PlayerScoreResponse;
import com.cardgame.model.entity.Card;
import com.cardgame.model.entity.Game;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("Realistic Game Flow Scenarios - Complete End-to-End")
class RealisticGameFlowIntegrationTest extends BaseIntegrationTest {

  @Test
  @DisplayName("Poker Game Flow - Single deck, 4 players, deal 5 cards each")
  void pokerGameFlow_singleDeck_fourPlayers_fiveCardsEach() {
    // Scenario: Texas Hold'em style game setup
    // Given - Setup game
    Game game = createGame();
    assertThat(game.getCardsRemaining()).isEqualTo(0);

    // When - Add deck
    addDeckToGame(game.getId());

    Game gameWithDeck = getGame(game.getId());
    assertThat(gameWithDeck.getTotalCardsInDeck()).isEqualTo(52);
    assertThat(gameWithDeck.getCardsRemaining()).isEqualTo(52);

    // When - Add 4 players
    String alice = addPlayer(game.getId(), "Alice");
    String bob = addPlayer(game.getId(), "Bob");
    String charlie = addPlayer(game.getId(), "Charlie");
    String diana = addPlayer(game.getId(), "Diana");

    Game gameWithPlayers = getGame(game.getId());
    assertThat(gameWithPlayers.getPlayersList()).hasSize(4);

    // When - Shuffle deck
    shuffleDeck(game.getId());

    // When - Deal 5 cards to each player
    List<Card> aliceCards = dealCards(game.getId(), alice, 5);
    List<Card> bobCards = dealCards(game.getId(), bob, 5);
    List<Card> charlieCards = dealCards(game.getId(), charlie, 5);
    List<Card> dianaCards = dealCards(game.getId(), diana, 5);

    // Then - Verify each player has 5 cards
    assertThat(aliceCards).hasSize(5);
    assertThat(bobCards).hasSize(5);
    assertThat(charlieCards).hasSize(5);
    assertThat(dianaCards).hasSize(5);

    // Then - Verify game state
    Game finalGame = getGame(game.getId());
    assertThat(finalGame.getCardsRemaining()).isEqualTo(32); // 52 - 20

    // Then - Get player scores
    List<PlayerScoreResponse> scores = getPlayerScores(game.getId());
    assertThat(scores).hasSize(4);

    // Verify all players appear in scores
    List<String> playerNames =
        scores.stream().map(PlayerScoreResponse::getName).collect(Collectors.toList());
    assertThat(playerNames).containsExactlyInAnyOrder("Alice", "Bob", "Charlie", "Diana");

    // Verify scores are sorted descending
    for (int i = 0; i < scores.size() - 1; i++) {
      assertThat(scores.get(i).getTotalValue())
          .isGreaterThanOrEqualTo(scores.get(i + 1).getTotalValue());
    }

    // Then - Check suit distribution in remaining cards
    Map<String, Integer> suitCounts = getSuitCounts(game.getId());
    int totalRemaining = suitCounts.values().stream().mapToInt(Integer::intValue).sum();
    assertThat(totalRemaining).isEqualTo(32);
  }

  @Test
  @DisplayName("Blackjack Flow - Multiple decks (shoe), deal rounds, shuffle mid-game")
  void blackjackGameFlow_multipleDecks_dealRounds_shuffleMidGame() {
    // Scenario: Casino Blackjack with 6-deck shoe
    // Given - Create game and add 6 decks (shoe)
    Game game = createGame();

    for (int i = 0; i < 6; i++) {
      addDeckToGame(game.getId());
    }

    Game gameWithShoe = getGame(game.getId());
    assertThat(gameWithShoe.getTotalCardsInDeck()).isEqualTo(312); // 6 × 52
    assertThat(gameWithShoe.getCardsRemaining()).isEqualTo(312);

    // When - Shuffle shoe
    shuffleDeck(game.getId());

    // When - Add 3 players
    String player1 = addPlayer(game.getId(), "Player1");
    String player2 = addPlayer(game.getId(), "Player2");
    String player3 = addPlayer(game.getId(), "Player3");

    // Round 1 - Deal 2 cards to each player
    dealCards(game.getId(), player1, 2);
    dealCards(game.getId(), player2, 2);
    dealCards(game.getId(), player3, 2);

    Game afterRound1 = getGame(game.getId());
    assertThat(afterRound1.getCardsRemaining()).isEqualTo(306); // 312 - 6

    // Round 2 - Player1 hits (1 card), Player2 hits twice (2 cards)
    dealCards(game.getId(), player1, 1);
    dealCards(game.getId(), player2, 2);

    Game afterRound2 = getGame(game.getId());
    assertThat(afterRound2.getCardsRemaining()).isEqualTo(303); // 306 - 3

    // Verify player card counts
    assertThat(getPlayerCards(game.getId(), player1)).hasSize(3); // 2 + 1
    assertThat(getPlayerCards(game.getId(), player2)).hasSize(4); // 2 + 2
    assertThat(getPlayerCards(game.getId(), player3)).hasSize(2); // 2 + 0

    // Shuffle mid-game (only affects remaining 303 cards)
    shuffleDeck(game.getId());

    Game afterMidShuffle = getGame(game.getId());
    assertThat(afterMidShuffle.getCardsRemaining()).isEqualTo(303); // Unchanged

    // Verify dealt cards unchanged
    assertThat(getPlayerCards(game.getId(), player1)).hasSize(3);
    assertThat(getPlayerCards(game.getId(), player2)).hasSize(4);
    assertThat(getPlayerCards(game.getId(), player3)).hasSize(2);

    // Round 3 - Deal from shuffled shoe
    dealCards(game.getId(), player3, 3);

    assertThat(getPlayerCards(game.getId(), player3)).hasSize(5); // 2 + 3

    // Get final scores
    List<PlayerScoreResponse> scores = getPlayerScores(game.getId());
    assertThat(scores).hasSize(3);

    // Verify total cards dealt
    int totalDealt = scores.stream().mapToInt(PlayerScoreResponse::getCardCount).sum();
    assertThat(totalDealt).isEqualTo(12); // 3 + 4 + 5

    Game finalGame = getGame(game.getId());
    assertThat(finalGame.getCardsRemaining()).isEqualTo(300); // 312 - 12
  }

  @Test
  @DisplayName("Complete Game Lifecycle - Create, play, player leaves, cleanup")
  void completeGameLifecycle_createPlayPlayerLeavesCleanup() {
    // Given - Create game and setup
    Game game = createGame();
    addDeckToGame(game.getId());
    addDeckToGame(game.getId()); // 2 decks = 104 cards

    String player1 = addPlayer(game.getId(), "Alice");
    String player2 = addPlayer(game.getId(), "Bob");
    String player3 = addPlayer(game.getId(), "Charlie");

    shuffleDeck(game.getId());

    // When - Play multiple rounds
    // Round 1
    dealCards(game.getId(), player1, 3);
    dealCards(game.getId(), player2, 3);
    dealCards(game.getId(), player3, 3);

    // Round 2
    dealCards(game.getId(), player1, 2);
    dealCards(game.getId(), player2, 2);
    dealCards(game.getId(), player3, 2);

    // Then - Verify state after rounds
    assertThat(getPlayerCards(game.getId(), player1)).hasSize(5);
    assertThat(getPlayerCards(game.getId(), player2)).hasSize(5);
    assertThat(getPlayerCards(game.getId(), player3)).hasSize(5);

    Game midGame = getGame(game.getId());
    assertThat(midGame.getCardsRemaining()).isEqualTo(89); // 104 - 15

    // Get scores after 2 rounds
    List<PlayerScoreResponse> midScores = getPlayerScores(game.getId());
    assertThat(midScores).hasSize(3);

    // When - Player 3 leaves (cards go to discard)
    removePlayer(game.getId(), player3);

    // Then - Verify player removed and cards discarded
    Game afterLeave = getGame(game.getId());
    assertThat(afterLeave.getPlayersList()).hasSize(2);
    assertThat(afterLeave.getDiscardedCards()).hasSize(5); // Charlie's 5 cards

    // Scores now show only 2 players
    List<PlayerScoreResponse> afterLeaveScores = getPlayerScores(game.getId());
    assertThat(afterLeaveScores).hasSize(2);

    // Round 3 - Continue with remaining players
    dealCards(game.getId(), player1, 2);
    dealCards(game.getId(), player2, 2);

    assertThat(getPlayerCards(game.getId(), player1)).hasSize(7);
    assertThat(getPlayerCards(game.getId(), player2)).hasSize(7);

    // Final scores
    List<PlayerScoreResponse> finalScores = getPlayerScores(game.getId());
    assertThat(finalScores).hasSize(2);

    // Verify game consistency
    Game finalGame = getGame(game.getId());
    assertThat(finalGame.getCardsRemaining()).isEqualTo(85); // 104 - 5 - 14
    assertThat(finalGame.getDiscardedCards()).hasSize(5);

    // When - Cleanup game
    deleteGame(game.getId());

    // Then - Verify game deleted
    ResponseEntity<Map<String, Object>> getResponse =
        restTemplate.exchange(
            baseUrl + "/games/" + game.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("Draw All Cards Scenario - Deal until deck exhausted")
  void drawAllCardsScenario_dealUntilDeckExhausted() {
    // Given - Small deck for exhaustion test
    Game game = createGame();
    addDeckToGame(game.getId()); // 52 cards

    String player1 = addPlayer(game.getId(), "Alice");
    String player2 = addPlayer(game.getId(), "Bob");

    shuffleDeck(game.getId());

    // When - Deal aggressively until deck exhausted
    // Alice gets 30 cards
    List<Card> aliceCards = dealCards(game.getId(), player1, 30);
    assertThat(aliceCards).hasSize(30);

    // Bob tries to get 30 cards but only 22 remain
    List<Card> bobCards = dealCards(game.getId(), player2, 30);
    assertThat(bobCards).hasSize(22); // Only 22 available

    // Then - Verify deck exhausted
    Game exhaustedGame = getGame(game.getId());
    assertThat(exhaustedGame.getCardsRemaining()).isEqualTo(0);

    // Try to deal more - should return empty
    List<Card> noCards = dealCards(game.getId(), player1, 1);
    assertThat(noCards).isEmpty();

    // Verify final state
    assertThat(getPlayerCards(game.getId(), player1)).hasSize(30);
    assertThat(getPlayerCards(game.getId(), player2)).hasSize(22);

    // Suit counts should be all zeros
    Map<String, Integer> suitCounts = getSuitCounts(game.getId());
    assertThat(suitCounts.values()).allMatch(count -> count == 0);

    // Card counts should be empty
    Map<String, Integer> cardCounts = getCardCounts(game.getId());
    assertThat(cardCounts).isEmpty();

    // Scores still work
    List<PlayerScoreResponse> scores = getPlayerScores(game.getId());
    assertThat(scores).hasSize(2);
    assertThat(scores.get(0).getCardCount() + scores.get(1).getCardCount()).isEqualTo(52);
  }

  @Test
  @DisplayName("Progressive Shuffle - Shuffle at different game stages")
  void progressiveShuffle_shuffleAtDifferentStages() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());

    String player = addPlayer(game.getId(), "Alice");

    // Shuffle 1 - Empty deck (should work)
    shuffleDeck(game.getId());

    // Add deck after shuffle
    addDeckToGame(game.getId());
    addDeckToGame(game.getId()); // Total: 156 cards (3 decks)

    // Shuffle 2 - Full shoe
    shuffleDeck(game.getId());

    // Deal some cards
    dealCards(game.getId(), player, 50);
    assertThat(getGame(game.getId()).getCardsRemaining()).isEqualTo(106);

    // Shuffle 3 - Partial deck
    shuffleDeck(game.getId());

    // Verify shuffle doesn't affect dealt cards
    assertThat(getPlayerCards(game.getId(), player)).hasSize(50);

    // Deal more
    dealCards(game.getId(), player, 50);
    assertThat(getGame(game.getId()).getCardsRemaining()).isEqualTo(56);

    // Shuffle 4 - Even fewer cards
    shuffleDeck(game.getId());

    // Deal rest
    List<Card> finalDeal = dealCards(game.getId(), player, 100);
    assertThat(finalDeal).hasSize(56); // Only 56 available

    // Shuffle 5 - Empty deck again
    shuffleDeck(game.getId());
    assertThat(getGame(game.getId()).getCardsRemaining()).isEqualTo(0);

    // Verify all cards dealt
    assertThat(getPlayerCards(game.getId(), player)).hasSize(156);
  }

  @Test
  @DisplayName("Query Operations Mid-Game - Verify queries during active game")
  void queryOperationsMidGame_verifyQueriesDuringActiveGame() {
    // Given - Setup active game
    Game game = createGame();
    addDeckToGame(game.getId());
    addDeckToGame(game.getId()); // 104 cards

    String player1 = addPlayer(game.getId(), "Alice");
    String player2 = addPlayer(game.getId(), "Bob");
    String player3 = addPlayer(game.getId(), "Charlie");

    shuffleDeck(game.getId());

    // Deal initial cards
    dealCards(game.getId(), player1, 7);
    dealCards(game.getId(), player2, 5);
    dealCards(game.getId(), player3, 3);

    // When/Then - Query 1: Player scores
    List<PlayerScoreResponse> scores1 = getPlayerScores(game.getId());
    assertThat(scores1).hasSize(3);
    assertThat(scores1.get(0).getCardCount()).isEqualTo(7);

    // When/Then - Query 2: Suit counts
    Map<String, Integer> suitCounts1 = getSuitCounts(game.getId());
    int remaining1 = suitCounts1.values().stream().mapToInt(Integer::intValue).sum();
    assertThat(remaining1).isEqualTo(89); // 104 - 15

    // When/Then - Query 3: Card counts
    Map<String, Integer> cardCounts1 = getCardCounts(game.getId());
    int cardsInDeck = cardCounts1.values().stream().mapToInt(Integer::intValue).sum();
    assertThat(cardsInDeck).isEqualTo(89);

    // Continue game - deal more cards
    dealCards(game.getId(), player1, 3);
    dealCards(game.getId(), player2, 5);

    // When/Then - Query 4: Updated scores
    List<PlayerScoreResponse> scores2 = getPlayerScores(game.getId());
    assertThat(scores2.get(0).getCardCount()).isIn(10, 10, 3); // One of them has 10

    // When/Then - Query 5: Updated counts
    Map<String, Integer> suitCounts2 = getSuitCounts(game.getId());
    int remaining2 = suitCounts2.values().stream().mapToInt(Integer::intValue).sum();
    assertThat(remaining2).isEqualTo(81); // 104 - 23

    // Remove a player
    removePlayer(game.getId(), player3);

    // When/Then - Query 6: Scores after player removal
    List<PlayerScoreResponse> scores3 = getPlayerScores(game.getId());
    assertThat(scores3).hasSize(2); // Only Alice and Bob

    // When/Then - Query 7: Game details
    Game finalGame = getGame(game.getId());
    assertThat(finalGame.getDiscardedCards()).hasSize(3); // Charlie's cards
    assertThat(finalGame.getPlayersList()).hasSize(2);
    assertThat(finalGame.getCardsRemaining()).isEqualTo(81); // Unchanged
  }
}
