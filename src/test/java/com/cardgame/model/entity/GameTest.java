package com.cardgame.model.entity;

import static org.assertj.core.api.Assertions.*;

import com.cardgame.exception.DeckAlreadyAddedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Game Entity - Unit Tests")
class GameTest {

  @Nested
  @DisplayName("removePlayer()")
  class RemovePlayerTests {

    @Test
    @DisplayName("Should remove player and increment playersRemoved counter")
    void removePlayer_whenPlayerExists_incrementsCounter() {
      // Given
      Game game = Game.createNew();
      Player player = Player.createNew("Alice");
      game.addPlayer(player);

      // When
      game.removePlayer(player.getId());

      // Then
      assertThat(game.getPlayers()).isEmpty();
      assertThat(game.getPlayersRemoved()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should move player's cards to discarded pile")
    void removePlayer_whenPlayerHasCards_movesCardsToDiscardPile() {
      // Given
      Game game = Game.createNew();
      Deck deck = Deck.createNew();
      game.addDeck(deck);

      Player player = Player.createNew("Alice");
      game.addPlayer(player);

      // Give player some cards
      player.getCards().add(game.getGameDeck().remove(0));
      player.getCards().add(game.getGameDeck().remove(0));
      player.getCards().add(game.getGameDeck().remove(0));

      // When
      game.removePlayer(player.getId());

      // Then
      assertThat(game.getPlayers()).isEmpty();
      assertThat(game.getDiscardedCards()).hasSize(3);
      assertThat(game.getCardsDiscarded()).isEqualTo(3);
      assertThat(game.getPlayersRemoved()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should accumulate playersRemoved counter on multiple removals")
    void removePlayer_whenMultiplePlayersRemoved_accumulatesCounter() {
      // Given
      Game game = Game.createNew();
      Player alice = Player.createNew("Alice");
      Player bob = Player.createNew("Bob");
      Player charlie = Player.createNew("Charlie");

      game.addPlayer(alice);
      game.addPlayer(bob);
      game.addPlayer(charlie);

      // When
      game.removePlayer(alice.getId());
      game.removePlayer(charlie.getId());

      // Then
      assertThat(game.getPlayers()).hasSize(1);
      assertThat(game.getPlayers().get(0).getName()).isEqualTo("Bob");
      assertThat(game.getPlayersRemoved()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should accumulate discarded cards from multiple players")
    void removePlayer_whenMultiplePlayersWithCards_accumulatesDiscardedCards() {
      // Given
      Game game = Game.createNew();
      Deck deck = Deck.createNew();
      game.addDeck(deck);

      Player alice = Player.createNew("Alice");
      Player bob = Player.createNew("Bob");

      game.addPlayer(alice);
      game.addPlayer(bob);

      // Deal cards
      for (int i = 0; i < 5; i++) alice.getCards().add(game.getGameDeck().remove(0));
      for (int i = 0; i < 3; i++) bob.getCards().add(game.getGameDeck().remove(0));

      int initialDeckSize = game.getGameDeck().size();

      // When
      game.removePlayer(alice.getId());
      game.removePlayer(bob.getId());

      // Then
      assertThat(game.getPlayers()).isEmpty();
      assertThat(game.getPlayersRemoved()).isEqualTo(2);
      assertThat(game.getDiscardedCards()).hasSize(8); // 5 + 3
      assertThat(game.getCardsDiscarded()).isEqualTo(8);
      assertThat(game.getGameDeck()).hasSize(initialDeckSize); // Deck unchanged
    }

    @Test
    @DisplayName("Should do nothing when removing non-existent player")
    void removePlayer_whenPlayerDoesNotExist_doesNothing() {
      // Given
      Game game = Game.createNew();
      Player alice = Player.createNew("Alice");
      game.addPlayer(alice);

      // When
      game.removePlayer("non-existent-player-id");

      // Then
      assertThat(game.getPlayers()).hasSize(1);
      assertThat(game.getPlayersRemoved()).isEqualTo(0);
      assertThat(game.getDiscardedCards()).isEmpty();
    }
  }

  @Nested
  @DisplayName("addDeck()")
  class AddDeckTests {

    @Test
    @DisplayName("Should add deck to game and track deck ID")
    void addDeck_whenDeckIsNew_addsDeckToGame() {
      // Given
      Game game = Game.createNew();
      Deck deck = Deck.createNew();

      // When
      game.addDeck(deck);

      // Then
      assertThat(game.getGameDeck()).hasSize(52);
      assertThat(game.getTotalDecksAdded()).isEqualTo(1);
      assertThat(game.getDeckIdsInUse()).contains(deck.getId());
    }

    @Test
    @DisplayName("Should throw exception when adding same deck twice")
    void addDeck_whenDeckAlreadyAdded_throwsException() {
      // Given
      Game game = Game.createNew();
      Deck deck = Deck.createNew();
      game.addDeck(deck);

      // When/Then
      assertThatThrownBy(() -> game.addDeck(deck))
          .isInstanceOf(DeckAlreadyAddedException.class)
          .hasMessageContaining(deck.getId())
          .hasMessageContaining(game.getId());

      // Verify state unchanged
      assertThat(game.getGameDeck()).hasSize(52);
      assertThat(game.getTotalDecksAdded()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should accumulate cards when adding multiple decks")
    void addDeck_whenMultipleDecksAdded_accumulatesCards() {
      // Given
      Game game = Game.createNew();
      Deck deck1 = Deck.createNew();
      Deck deck2 = Deck.createNew();

      // When
      game.addDeck(deck1);
      game.addDeck(deck2);

      // Then
      assertThat(game.getGameDeck()).hasSize(104); // 52 * 2
      assertThat(game.getTotalDecksAdded()).isEqualTo(2);
      assertThat(game.getDeckIdsInUse()).containsExactlyInAnyOrder(deck1.getId(), deck2.getId());
    }
  }

  @Nested
  @DisplayName("Computed Properties")
  class ComputedPropertiesTests {

    @Test
    @DisplayName("getTotalCardsInDeck() should return total based on decks added")
    void getTotalCardsInDeck_returnsCorrectTotal() {
      // Given
      Game game = Game.createNew();

      // When/Then - No decks
      assertThat(game.getTotalCardsInDeck()).isEqualTo(0);

      // When/Then - One deck
      game.addDeck(Deck.createNew());
      assertThat(game.getTotalCardsInDeck()).isEqualTo(52);

      // When/Then - Two decks
      game.addDeck(Deck.createNew());
      assertThat(game.getTotalCardsInDeck()).isEqualTo(104);
    }

    @Test
    @DisplayName("getCardsRemaining() should return current deck size")
    void getCardsRemaining_returnsCurrentDeckSize() {
      // Given
      Game game = Game.createNew();
      Deck deck = Deck.createNew();
      game.addDeck(deck);

      // When/Then - Full deck
      assertThat(game.getCardsRemaining()).isEqualTo(52);

      // When/Then - After dealing some cards
      game.getGameDeck().remove(0);
      game.getGameDeck().remove(0);
      game.getGameDeck().remove(0);
      assertThat(game.getCardsRemaining()).isEqualTo(49);
    }

    @Test
    @DisplayName("getCardsDiscarded() should return discarded pile size")
    void getCardsDiscarded_returnsDiscardedPileSize() {
      // Given
      Game game = Game.createNew();
      Deck deck = Deck.createNew();
      game.addDeck(deck);

      Player alice = Player.createNew("Alice");
      Player bob = Player.createNew("Bob");

      game.addPlayer(alice);
      game.addPlayer(bob);

      // Deal cards
      for (int i = 0; i < 3; i++) alice.getCards().add(game.getGameDeck().remove(0));
      for (int i = 0; i < 5; i++) bob.getCards().add(game.getGameDeck().remove(0));

      // When/Then - No players removed yet
      assertThat(game.getCardsDiscarded()).isEqualTo(0);

      // When/Then - One player removed
      game.removePlayer(alice.getId());
      assertThat(game.getCardsDiscarded()).isEqualTo(3);

      // When/Then - Both players removed
      game.removePlayer(bob.getId());
      assertThat(game.getCardsDiscarded()).isEqualTo(8);
    }
  }

  @Nested
  @DisplayName("Player Management")
  class PlayerManagementTests {

    @Test
    @DisplayName("hasPlayer() should return true when player exists")
    void hasPlayer_whenPlayerExists_returnsTrue() {
      // Given
      Game game = Game.createNew();
      Player player = Player.createNew("Alice");
      game.addPlayer(player);

      // When/Then
      assertThat(game.hasPlayer(player.getId())).isTrue();
    }

    @Test
    @DisplayName("hasPlayer() should return false when player does not exist")
    void hasPlayer_whenPlayerDoesNotExist_returnsFalse() {
      // Given
      Game game = Game.createNew();

      // When/Then
      assertThat(game.hasPlayer("non-existent-id")).isFalse();
    }

    @Test
    @DisplayName("hasPlayerByName() should be case-insensitive")
    void hasPlayerByName_isCaseInsensitive() {
      // Given
      Game game = Game.createNew();
      Player player = Player.createNew("Alice");
      game.addPlayer(player);

      // When/Then
      assertThat(game.hasPlayerByName("alice")).isTrue();
      assertThat(game.hasPlayerByName("ALICE")).isTrue();
      assertThat(game.hasPlayerByName("Alice")).isTrue();
      assertThat(game.hasPlayerByName("Bob")).isFalse();
    }

    @Test
    @DisplayName("getPlayer() should return player when exists")
    void getPlayer_whenPlayerExists_returnsPlayer() {
      // Given
      Game game = Game.createNew();
      Player player = Player.createNew("Alice");
      game.addPlayer(player);

      // When
      Player result = game.getPlayer(player.getId());

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(player.getId());
      assertThat(result.getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("getPlayer() should return null when player does not exist")
    void getPlayer_whenPlayerDoesNotExist_returnsNull() {
      // Given
      Game game = Game.createNew();

      // When
      Player result = game.getPlayer("non-existent-id");

      // Then
      assertThat(result).isNull();
    }
  }

  @Nested
  @DisplayName("Game Initialization")
  class GameInitializationTests {

    @Test
    @DisplayName("createNew() should initialize with default values")
    void createNew_initializesWithDefaults() {
      // When
      Game game = Game.createNew();

      // Then
      assertThat(game.getId()).isNotNull();
      assertThat(game.getPlayers()).isEmpty();
      assertThat(game.getGameDeck()).isEmpty();
      assertThat(game.getDiscardedCards()).isEmpty();
      assertThat(game.getTotalDecksAdded()).isEqualTo(0);
      assertThat(game.getPlayersRemoved()).isEqualTo(0);
      assertThat(game.getCardsRemaining()).isEqualTo(0);
      assertThat(game.getCardsDiscarded()).isEqualTo(0);
      assertThat(game.getTotalCardsInDeck()).isEqualTo(0);
      assertThat(game.getDeckIdsInUse()).isEmpty();
    }

    @Test
    @DisplayName("createNew() should generate unique IDs")
    void createNew_generatesUniqueIds() {
      // When
      Game game1 = Game.createNew();
      Game game2 = Game.createNew();

      // Then
      assertThat(game1.getId()).isNotEqualTo(game2.getId());
    }
  }
}
