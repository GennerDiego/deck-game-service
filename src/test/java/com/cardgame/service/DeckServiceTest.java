package com.cardgame.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.cardgame.exception.DeckInUseException;
import com.cardgame.exception.DeckNotFoundException;
import com.cardgame.exception.GameNotFoundException;
import com.cardgame.model.entity.Deck;
import com.cardgame.model.entity.Game;
import com.cardgame.model.entity.Rank;
import com.cardgame.model.entity.Suit;
import com.cardgame.repository.DeckRepository;
import com.cardgame.repository.GameRepository;
import com.cardgame.util.ShuffleUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
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

  @Mock private DeckRepository deckRepository;

  @Mock private GameRepository gameRepository;

  @Mock private GameService gameService;

  @Mock private ShuffleUtil shuffleUtil;

  @Mock private DistributedLockService lockService;

  @InjectMocks private DeckService deckService;

  @BeforeEach
  void setUp() {
    // Mock lockService to execute the operation directly (bypass actual locking in unit tests)
    // Use lenient() because not all test methods use locking
    lenient()
        .when(lockService.executeWithLock(anyString(), any(Supplier.class)))
        .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(1)).get());

    // Mock void version for operations that don't return values
    lenient()
        .doAnswer(
            invocation -> {
              ((Runnable) invocation.getArgument(1)).run();
              return null;
            })
        .when(lockService)
        .executeWithLock(anyString(), any(Runnable.class));
  }

  @Nested
  @DisplayName("createDeck()")
  class CreateDeckTests {

    @Test
    @DisplayName("Should create and persist a new standard deck")
    void createDeck_shouldCreateStandardDeck() {
      // Given
      Deck deck = Deck.createNew();
      when(deckRepository.save(any(Deck.class))).thenReturn(deck);

      // When
      Deck createdDeck = deckService.createDeck();

      // Then
      assertThat(createdDeck).isNotNull();
      assertThat(createdDeck.getId()).isNotNull();
      assertThat(createdDeck.getCards()).hasSize(52);

      verify(deckRepository, times(1)).save(any(Deck.class));
    }
  }

  @Nested
  @DisplayName("findById()")
  class FindByIdTests {

    @Test
    @DisplayName("Should return deck when deck exists")
    void findById_whenDeckExists_returnsDeck() {
      // Given
      String deckId = "deck-123";
      Deck deck = Deck.builder().id(deckId).build();
      when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));

      // When
      Deck foundDeck = deckService.findById(deckId);

      // Then
      assertThat(foundDeck).isNotNull();
      assertThat(foundDeck.getId()).isEqualTo(deckId);

      verify(deckRepository, times(1)).findById(deckId);
    }

    @Test
    @DisplayName("Should throw DeckNotFoundException when deck does not exist")
    void findById_whenDeckNotFound_throwsException() {
      // Given
      String deckId = "invalid-deck-id";
      when(deckRepository.findById(deckId)).thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> deckService.findById(deckId))
          .isInstanceOf(DeckNotFoundException.class)
          .hasMessageContaining("invalid-deck-id");
    }
  }

  @Nested
  @DisplayName("getAllDecks()")
  class GetAllDecksTests {

    @Test
    @DisplayName("Should return empty list when no decks exist")
    void getAllDecks_whenNoDecks_returnsEmptyList() {
      // Given
      when(deckRepository.findAll()).thenReturn(List.of());

      // When
      List<Deck> decks = deckService.getAllDecks();

      // Then
      assertThat(decks).isEmpty();
    }

    @Test
    @DisplayName("Should return all decks")
    void getAllDecks_whenDecksExist_returnsAllDecks() {
      // Given
      Deck deck1 = Deck.builder().id("deck-1").build();
      Deck deck2 = Deck.builder().id("deck-2").build();
      when(deckRepository.findAll()).thenReturn(List.of(deck1, deck2));

      // When
      List<Deck> decks = deckService.getAllDecks();

      // Then
      assertThat(decks).hasSize(2);
      assertThat(decks).extracting(Deck::getId).containsExactly("deck-1", "deck-2");
    }
  }

  @Nested
  @DisplayName("addDeckToGame(gameId, deckId)")
  class AddDeckToGameTests {

    @Test
    @DisplayName("Should add existing deck to game")
    void addDeckToGame_withExistingDeck_addsToGame() {
      // Given
      String gameId = "game-123";
      String deckId = "deck-456";

      Game game = Game.createNew();
      Deck deck = Deck.createNew();
      deck.setId(deckId);

      when(gameService.findById(gameId)).thenReturn(game);
      when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));
      when(gameRepository.save(any(Game.class))).thenReturn(game);

      // When
      deckService.addDeckToGame(gameId, deckId);

      // Then
      assertThat(game.getGameDeck()).hasSize(52);
      assertThat(game.getTotalCardsInDeck()).isEqualTo(52);
      assertThat(game.getDeckIdsInUse()).contains(deckId);

      verify(gameService, times(1)).findById(gameId);
      verify(deckRepository, times(1)).findById(deckId);
      verify(gameRepository, times(1)).save(game);
    }

    @Test
    @DisplayName("Should add multiple decks to game (shoe)")
    void addDeckToGame_calledMultipleTimes_createsShoe() {
      // Given
      String gameId = "game-123";
      String deckId1 = "deck-1";
      String deckId2 = "deck-2";
      String deckId3 = "deck-3";

      Game game = Game.createNew();
      Deck deck1 = Deck.createNew();
      deck1.setId(deckId1);
      Deck deck2 = Deck.createNew();
      deck2.setId(deckId2);
      Deck deck3 = Deck.createNew();
      deck3.setId(deckId3);

      when(gameService.findById(gameId)).thenReturn(game);
      when(deckRepository.findById(deckId1)).thenReturn(Optional.of(deck1));
      when(deckRepository.findById(deckId2)).thenReturn(Optional.of(deck2));
      when(deckRepository.findById(deckId3)).thenReturn(Optional.of(deck3));
      when(gameRepository.save(any(Game.class))).thenReturn(game);

      // When
      deckService.addDeckToGame(gameId, deckId1);
      deckService.addDeckToGame(gameId, deckId2);
      deckService.addDeckToGame(gameId, deckId3);

      // Then
      assertThat(game.getGameDeck()).hasSize(156); // 3 decks × 52 cards
      assertThat(game.getTotalCardsInDeck()).isEqualTo(156);
      assertThat(game.getDeckIdsInUse()).containsExactlyInAnyOrder(deckId1, deckId2, deckId3);

      verify(gameService, times(3)).findById(gameId);
      verify(gameRepository, times(3)).save(game);
    }

    @Test
    @DisplayName("Should throw DeckAlreadyAddedException when same deck added twice")
    void addDeckToGame_whenDeckAlreadyAdded_throwsException() {
      // Given
      String gameId = "game-123";
      String deckId = "deck-456";

      Game game = Game.createNew();
      Deck deck = Deck.createNew();
      deck.setId(deckId);

      when(gameService.findById(gameId)).thenReturn(game);
      when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));
      when(gameRepository.save(any(Game.class))).thenReturn(game);

      // When - Add deck first time (should succeed)
      deckService.addDeckToGame(gameId, deckId);

      // Then - Add same deck second time (should fail)
      assertThatThrownBy(() -> deckService.addDeckToGame(gameId, deckId))
          .isInstanceOf(com.cardgame.exception.DeckAlreadyAddedException.class)
          .hasMessageContaining(deckId)
          .hasMessageContaining(game.getId());

      // Verify deck was only saved once
      verify(gameRepository, times(1)).save(game);
    }

    @Test
    @DisplayName("Should throw GameNotFoundException when game does not exist")
    void addDeckToGame_whenGameNotFound_throwsException() {
      // Given
      String gameId = "invalid-game-id";
      String deckId = "deck-456";
      when(gameService.findById(gameId)).thenThrow(new GameNotFoundException(gameId));

      // When/Then
      assertThatThrownBy(() -> deckService.addDeckToGame(gameId, deckId))
          .isInstanceOf(GameNotFoundException.class)
          .hasMessageContaining("invalid-game-id");

      verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DeckNotFoundException when deck does not exist")
    void addDeckToGame_whenDeckNotFound_throwsException() {
      // Given
      String gameId = "game-123";
      String deckId = "invalid-deck-id";

      Game game = Game.createNew();
      when(gameService.findById(gameId)).thenReturn(game);
      when(deckRepository.findById(deckId)).thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> deckService.addDeckToGame(gameId, deckId))
          .isInstanceOf(DeckNotFoundException.class)
          .hasMessageContaining("invalid-deck-id");

      verify(gameRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("deleteDeck()")
  class DeleteDeckTests {

    @Test
    @DisplayName("Should delete deck when not in use by any game")
    void deleteDeck_whenNotInUse_deletesDeck() {
      // Given
      String deckId = "deck-123";
      Deck deck = Deck.builder().id(deckId).build();

      when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));
      when(gameRepository.findAll()).thenReturn(List.of());

      // When
      deckService.deleteDeck(deckId);

      // Then
      verify(deckRepository, times(1)).findById(deckId);
      verify(gameRepository, times(1)).findAll();
      verify(deckRepository, times(1)).deleteById(deckId);
    }

    @Test
    @DisplayName("Should throw DeckInUseException when deck is in use by a game")
    void deleteDeck_whenInUse_throwsException() {
      // Given
      String deckId = "deck-123";
      Deck deck = Deck.builder().id(deckId).build();

      Set<String> deckIds = new HashSet<>();
      deckIds.add(deckId);

      Game game = Game.builder().id("game-1").deckIdsInUse(deckIds).build();

      when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));
      when(gameRepository.findAll()).thenReturn(List.of(game));

      // When/Then
      assertThatThrownBy(() -> deckService.deleteDeck(deckId))
          .isInstanceOf(DeckInUseException.class)
          .hasMessageContaining("deck-123")
          .hasMessageContaining("in use by a game");

      verify(deckRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Should throw DeckNotFoundException when deck does not exist")
    void deleteDeck_whenDeckNotFound_throwsException() {
      // Given
      String deckId = "invalid-deck-id";
      when(deckRepository.findById(deckId)).thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> deckService.deleteDeck(deckId))
          .isInstanceOf(DeckNotFoundException.class)
          .hasMessageContaining("invalid-deck-id");

      verify(gameRepository, never()).findAll();
      verify(deckRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Should delete deck when other games exist but don't use this deck")
    void deleteDeck_whenOtherGamesExistButDontUseDeck_deletesDeck() {
      // Given
      String deckId = "deck-123";
      Deck deck = Deck.builder().id(deckId).build();

      Set<String> otherDeckIds = new HashSet<>();
      otherDeckIds.add("deck-456");
      otherDeckIds.add("deck-789");

      Game game = Game.builder().id("game-1").deckIdsInUse(otherDeckIds).build();

      when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));
      when(gameRepository.findAll()).thenReturn(List.of(game));

      // When
      deckService.deleteDeck(deckId);

      // Then
      verify(deckRepository, times(1)).deleteById(deckId);
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
