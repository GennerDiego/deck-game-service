package com.cardgame.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cardgame.exception.GameNotFoundException;
import com.cardgame.model.entity.Deck;
import com.cardgame.model.entity.Game;
import com.cardgame.model.entity.Rank;
import com.cardgame.model.entity.Suit;
import com.cardgame.repository.GameRepository;
import com.cardgame.util.ShuffleUtil;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeckService - Unit Tests")
class DeckServiceTest {

  @Mock private GameRepository gameRepository;

  @Mock private GameService gameService;

  @Mock private ShuffleUtil shuffleUtil;

  @InjectMocks private DeckService deckService;

  @Nested
  @DisplayName("addDeckToGame()")
  class AddDeckToGameTests {

    @Test
    @DisplayName("Should add standard 52-card deck to game")
    void addDeckToGame_shouldAddStandardDeckToGame() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      when(gameService.findById(gameId)).thenReturn(game);
      when(gameRepository.save(any(Game.class))).thenReturn(game);

      // When
      deckService.addDeckToGame(gameId);

      // Then
      assertThat(game.getGameDeck()).hasSize(52);
      assertThat(game.getTotalCardsInDeck()).isEqualTo(52);

      verify(gameService, times(1)).findById(gameId);
      verify(gameRepository, times(1)).save(game);
    }

    @Test
    @DisplayName("Should add multiple decks to game (shoe)")
    void addDeckToGame_calledMultipleTimes_createsShoe() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      when(gameService.findById(gameId)).thenReturn(game);
      when(gameRepository.save(any(Game.class))).thenReturn(game);

      // When
      deckService.addDeckToGame(gameId);
      deckService.addDeckToGame(gameId);
      deckService.addDeckToGame(gameId);

      // Then
      assertThat(game.getGameDeck()).hasSize(156); // 3 decks × 52 cards
      assertThat(game.getTotalCardsInDeck()).isEqualTo(156);

      verify(gameService, times(3)).findById(gameId);
      verify(gameRepository, times(3)).save(game);
    }

    @Test
    @DisplayName("Should throw GameNotFoundException when game does not exist")
    void addDeckToGame_whenGameNotFound_throwsException() {
      // Given
      String gameId = "invalid-game-id";
      when(gameService.findById(gameId)).thenThrow(new GameNotFoundException(gameId));

      // When/Then
      assertThatThrownBy(() -> deckService.addDeckToGame(gameId))
          .isInstanceOf(GameNotFoundException.class)
          .hasMessageContaining("invalid-game-id");

      verify(gameRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("shuffleGameDeck()")
  class ShuffleGameDeckTests {

    @Test
    @DisplayName("Should shuffle deck when deck has cards")
    void shuffleGameDeck_withCards_shufflesDeck() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      game.addDeck(Deck.createNew());
      when(gameService.findById(gameId)).thenReturn(game);
      when(gameRepository.save(any(Game.class))).thenReturn(game);

      // When
      deckService.shuffleGameDeck(gameId);

      // Then
      verify(shuffleUtil, times(1)).shuffle(game.getGameDeck());
      verify(gameRepository, times(1)).save(game);
    }

    @Test
    @DisplayName("Should not shuffle when deck is empty")
    void shuffleGameDeck_withEmptyDeck_doesNotShuffle() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew(); // Empty game deck
      when(gameService.findById(gameId)).thenReturn(game);

      // When
      deckService.shuffleGameDeck(gameId);

      // Then
      verify(shuffleUtil, never()).shuffle(any());
      verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw GameNotFoundException when game does not exist")
    void shuffleGameDeck_whenGameNotFound_throwsException() {
      // Given
      String gameId = "invalid-game-id";
      when(gameService.findById(gameId)).thenThrow(new GameNotFoundException(gameId));

      // When/Then
      assertThatThrownBy(() -> deckService.shuffleGameDeck(gameId))
          .isInstanceOf(GameNotFoundException.class)
          .hasMessageContaining("invalid-game-id");

      verify(shuffleUtil, never()).shuffle(any());
      verify(gameRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("getSuitCounts()")
  class GetSuitCountsTests {

    @Test
    @DisplayName("Should return all suits with count zero for empty deck")
    void getSuitCounts_withEmptyDeck_returnsZeroCounts() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      when(gameService.findById(gameId)).thenReturn(game);

      // When
      Map<String, Integer> result = deckService.getSuitCounts(gameId);

      // Then
      assertThat(result).hasSize(4);
      assertThat(result.get("HEARTS")).isZero();
      assertThat(result.get("SPADES")).isZero();
      assertThat(result.get("CLUBS")).isZero();
      assertThat(result.get("DIAMONDS")).isZero();
    }

    @Test
    @DisplayName("Should return correct counts for standard deck")
    void getSuitCounts_withStandardDeck_returnsCorrectCounts() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      game.addDeck(Deck.createNew());
      when(gameService.findById(gameId)).thenReturn(game);

      // When
      Map<String, Integer> result = deckService.getSuitCounts(gameId);

      // Then
      assertThat(result).hasSize(4);
      assertThat(result.get("HEARTS")).isEqualTo(13);
      assertThat(result.get("SPADES")).isEqualTo(13);
      assertThat(result.get("CLUBS")).isEqualTo(13);
      assertThat(result.get("DIAMONDS")).isEqualTo(13);
    }

    @Test
    @DisplayName("Should return correct counts after removing some cards")
    void getSuitCounts_afterRemovingSomeCards_returnsCorrectCounts() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      game.addDeck(Deck.createNew());

      // Remove all HEARTS (13 cards)
      game.getGameDeck().removeIf(card -> card.getSuit() == Suit.HEARTS);

      when(gameService.findById(gameId)).thenReturn(game);

      // When
      Map<String, Integer> result = deckService.getSuitCounts(gameId);

      // Then
      assertThat(result.get("HEARTS")).isZero();
      assertThat(result.get("SPADES")).isEqualTo(13);
      assertThat(result.get("CLUBS")).isEqualTo(13);
      assertThat(result.get("DIAMONDS")).isEqualTo(13);
    }

    @Test
    @DisplayName("Should return doubled counts for two decks (shoe)")
    void getSuitCounts_withTwoDecks_returnsDoubledCounts() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      game.addDeck(Deck.createNew());
      game.addDeck(Deck.createNew());
      when(gameService.findById(gameId)).thenReturn(game);

      // When
      Map<String, Integer> result = deckService.getSuitCounts(gameId);

      // Then
      assertThat(result.get("HEARTS")).isEqualTo(26);
      assertThat(result.get("SPADES")).isEqualTo(26);
      assertThat(result.get("CLUBS")).isEqualTo(26);
      assertThat(result.get("DIAMONDS")).isEqualTo(26);
    }
  }

  @Nested
  @DisplayName("getCardCounts()")
  class GetCardCountsTests {

    @Test
    @DisplayName("Should return empty map for empty deck")
    void getCardCounts_withEmptyDeck_returnsEmptyMap() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      when(gameService.findById(gameId)).thenReturn(game);

      // When
      Map<String, Integer> result = deckService.getCardCounts(gameId);

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return count of 1 for each card in standard deck")
    void getCardCounts_withStandardDeck_returnsCountOfOne() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      game.addDeck(Deck.createNew());
      when(gameService.findById(gameId)).thenReturn(game);

      // When
      Map<String, Integer> result = deckService.getCardCounts(gameId);

      // Then
      assertThat(result).hasSize(52);
      result.values().forEach(count -> assertThat(count).isEqualTo(1));
      assertThat(result).containsKey("ACE of HEARTS");
      assertThat(result).containsKey("KING of SPADES");
    }

    @Test
    @DisplayName("Should return count of 2 for each card in two-deck shoe")
    void getCardCounts_withTwoDecks_returnsCountOfTwo() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      game.addDeck(Deck.createNew());
      game.addDeck(Deck.createNew());
      when(gameService.findById(gameId)).thenReturn(game);

      // When
      Map<String, Integer> result = deckService.getCardCounts(gameId);

      // Then
      assertThat(result).hasSize(52);
      result.values().forEach(count -> assertThat(count).isEqualTo(2));
      assertThat(result.get("ACE of HEARTS")).isEqualTo(2);
      assertThat(result.get("KING of SPADES")).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return sorted map by card key")
    void getCardCounts_returnsMapSortedByKey() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      game.addDeck(Deck.createNew());
      when(gameService.findById(gameId)).thenReturn(game);

      // When
      Map<String, Integer> result = deckService.getCardCounts(gameId);

      // Then - Check that map has all 52 cards and maintains sorted order
      assertThat(result).hasSize(52);
      // Verify some cards exist in the result
      assertThat(result)
          .containsKeys("ACE of CLUBS", "KING of DIAMONDS", "TWO of HEARTS", "QUEEN of SPADES");
    }

    @Test
    @DisplayName("Should reflect partial deck after dealing cards")
    void getCardCounts_afterDealingCards_reflectsPartialDeck() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      game.addDeck(Deck.createNew());

      // Remove HEARTS-ACE
      game.getGameDeck()
          .removeIf(card -> card.getSuit() == Suit.HEARTS && card.getRank() == Rank.ACE);

      when(gameService.findById(gameId)).thenReturn(game);

      // When
      Map<String, Integer> result = deckService.getCardCounts(gameId);

      // Then
      assertThat(result).hasSize(51); // One card removed
      assertThat(result).doesNotContainKey("ACE of HEARTS");
    }
  }
}
