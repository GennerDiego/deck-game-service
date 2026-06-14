package com.cardgame.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cardgame.exception.*;
import com.cardgame.model.dto.PlayerScoreResponse;
import com.cardgame.model.entity.Card;
import com.cardgame.model.entity.Deck;
import com.cardgame.model.entity.Game;
import com.cardgame.model.entity.Player;
import com.cardgame.repository.GameRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerService - Unit Tests")
class PlayerServiceTest {

  @Mock private GameRepository gameRepository;

  @Mock private GameService gameService;

  @InjectMocks private PlayerService playerService;

  @Nested
  @DisplayName("addPlayer()")
  class AddPlayerTests {

    @Test
    @DisplayName("Should add player to game successfully")
    void addPlayer_withValidPlayer_addsPlayerToGame() {
      // Given
      String gameId = "game-123";
      Player player = Player.createNew("Alice");
      Game game = Game.createNew();
      when(gameService.findById(gameId)).thenReturn(game);
      when(gameRepository.save(any(Game.class))).thenReturn(game);

      // When
      playerService.addPlayer(gameId, player);

      // Then
      assertThat(game.getPlayers()).hasSize(1);
      assertThat(game.getPlayers()).contains(player);

      verify(gameService, times(1)).findById(gameId);
      verify(gameRepository, times(1)).save(game);
    }

    @Test
    @DisplayName("Should throw DuplicatePlayerException when player already exists")
    void addPlayer_whenPlayerAlreadyExists_throwsException() {
      // Given
      String gameId = "game-123";
      Player player = Player.createNew("Alice");
      Game game = Game.createNew();
      game.addPlayer(player);
      when(gameService.findById(gameId)).thenReturn(game);

      // When/Then
      assertThatThrownBy(() -> playerService.addPlayer(gameId, player))
          .isInstanceOf(DuplicatePlayerException.class)
          .hasMessageContaining(player.getId())
          .hasMessageContaining(gameId);

      verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw GameNotFoundException when game does not exist")
    void addPlayer_whenGameNotFound_throwsException() {
      // Given
      String gameId = "invalid-game-id";
      Player player = Player.createNew("Alice");
      when(gameService.findById(gameId)).thenThrow(new GameNotFoundException(gameId));

      // When/Then
      assertThatThrownBy(() -> playerService.addPlayer(gameId, player))
          .isInstanceOf(GameNotFoundException.class)
          .hasMessageContaining(gameId);

      verify(gameRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("removePlayer()")
  class RemovePlayerTests {

    @Test
    @DisplayName("Should remove player from game successfully")
    void removePlayer_whenPlayerExists_removesPlayer() {
      // Given
      String gameId = "game-123";
      Player player = Player.createNew("Alice");
      String playerId = player.getId();
      Game game = Game.createNew();
      game.addPlayer(player);
      when(gameService.findById(gameId)).thenReturn(game);
      when(gameRepository.save(any(Game.class))).thenReturn(game);

      // When
      playerService.removePlayer(gameId, playerId);

      // Then
      assertThat(game.getPlayers()).isEmpty();

      verify(gameService, times(1)).findById(gameId);
      verify(gameRepository, times(1)).save(game);
    }

    @Test
    @DisplayName("Should throw PlayerNotFoundException when player does not exist")
    void removePlayer_whenPlayerNotFound_throwsException() {
      // Given
      String gameId = "game-123";
      String playerId = "invalid-player-id";
      Game game = Game.createNew();
      when(gameService.findById(gameId)).thenReturn(game);

      // When/Then
      assertThatThrownBy(() -> playerService.removePlayer(gameId, playerId))
          .isInstanceOf(PlayerNotFoundException.class)
          .hasMessageContaining(playerId)
          .hasMessageContaining(gameId);

      verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw GameNotFoundException when game does not exist")
    void removePlayer_whenGameNotFound_throwsException() {
      // Given
      String gameId = "invalid-game-id";
      String playerId = "player-123";
      when(gameService.findById(gameId)).thenThrow(new GameNotFoundException(gameId));

      // When/Then
      assertThatThrownBy(() -> playerService.removePlayer(gameId, playerId))
          .isInstanceOf(GameNotFoundException.class)
          .hasMessageContaining(gameId);

      verify(gameRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("dealCards()")
  class DealCardsTests {

    @Test
    @DisplayName("Should deal requested number of cards successfully")
    void dealCards_withValidCount_dealsCards() {
      // Given
      String gameId = "game-123";
      Player player = Player.createNew("Alice");
      String playerId = player.getId();
      Game game = Game.createNew();
      game.addPlayer(player);
      game.addDeck(Deck.createNew());
      when(gameService.findById(gameId)).thenReturn(game);
      when(gameRepository.save(any(Game.class))).thenReturn(game);

      // When
      List<Card> dealtCards = playerService.dealCards(gameId, playerId, 5);

      // Then
      assertThat(dealtCards).hasSize(5);
      assertThat(player.getCards()).hasSize(5);
      assertThat(game.getGameDeck()).hasSize(47); // 52 - 5

      verify(gameService, times(1)).findById(gameId);
      verify(gameRepository, times(1)).save(game);
    }

    @Test
    @DisplayName("Should deal all available cards when requesting more than available")
    void dealCards_requestingMoreThanAvailable_dealsOnlyAvailableCards() {
      // Given
      String gameId = "game-123";
      Player player = Player.createNew("Alice");
      String playerId = player.getId();
      Game game = Game.createNew();
      game.addPlayer(player);
      game.addDeck(Deck.createNew());
      when(gameService.findById(gameId)).thenReturn(game);
      when(gameRepository.save(any(Game.class))).thenReturn(game);

      // When
      List<Card> dealtCards = playerService.dealCards(gameId, playerId, 100);

      // Then
      assertThat(dealtCards).hasSize(52); // Only 52 available
      assertThat(player.getCards()).hasSize(52);
      assertThat(game.getGameDeck()).isEmpty();

      verify(gameRepository, times(1)).save(game);
    }

    @Test
    @DisplayName("Should return empty list when deck is empty")
    void dealCards_withEmptyDeck_returnsEmptyList() {
      // Given
      String gameId = "game-123";
      Player player = Player.createNew("Alice");
      String playerId = player.getId();
      Game game = Game.createNew();
      game.addPlayer(player);
      // No deck added - empty game deck
      when(gameService.findById(gameId)).thenReturn(game);
      when(gameRepository.save(any(Game.class))).thenReturn(game);

      // When
      List<Card> dealtCards = playerService.dealCards(gameId, playerId, 5);

      // Then
      assertThat(dealtCards).isEmpty();
      assertThat(player.getCards()).isEmpty();

      verify(gameRepository, times(1)).save(game);
    }

    @Test
    @DisplayName("Should throw InvalidDealOperationException when count is zero")
    void dealCards_withZeroCount_throwsException() {
      // Given
      String gameId = "game-123";
      String playerId = "player-123";

      // When/Then
      assertThatThrownBy(() -> playerService.dealCards(gameId, playerId, 0))
          .isInstanceOf(InvalidDealOperationException.class)
          .hasMessageContaining("count");

      verify(gameService, never()).findById(any());
      verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidDealOperationException when count is negative")
    void dealCards_withNegativeCount_throwsException() {
      // Given
      String gameId = "game-123";
      String playerId = "player-123";

      // When/Then
      assertThatThrownBy(() -> playerService.dealCards(gameId, playerId, -5))
          .isInstanceOf(InvalidDealOperationException.class)
          .hasMessageContaining("count");

      verify(gameService, never()).findById(any());
      verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw PlayerNotFoundException when player does not exist")
    void dealCards_whenPlayerNotFound_throwsException() {
      // Given
      String gameId = "game-123";
      String playerId = "invalid-player-id";
      Game game = Game.createNew();
      when(gameService.findById(gameId)).thenReturn(game);

      // When/Then
      assertThatThrownBy(() -> playerService.dealCards(gameId, playerId, 5))
          .isInstanceOf(PlayerNotFoundException.class)
          .hasMessageContaining(playerId)
          .hasMessageContaining(gameId);

      verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw GameNotFoundException when game does not exist")
    void dealCards_whenGameNotFound_throwsException() {
      // Given
      String gameId = "invalid-game-id";
      String playerId = "player-123";
      when(gameService.findById(gameId)).thenThrow(new GameNotFoundException(gameId));

      // When/Then
      assertThatThrownBy(() -> playerService.dealCards(gameId, playerId, 5))
          .isInstanceOf(GameNotFoundException.class)
          .hasMessageContaining(gameId);

      verify(gameRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("getPlayerCards()")
  class GetPlayerCardsTests {

    @Test
    @DisplayName("Should return player cards when player has cards")
    void getPlayerCards_whenPlayerHasCards_returnsCards() {
      // Given
      String gameId = "game-123";
      Player player = Player.createNew("Alice");
      String playerId = player.getId();
      Game game = Game.createNew();
      game.addPlayer(player);
      game.addDeck(Deck.createNew());

      // Deal 5 cards to player
      for (int i = 0; i < 5; i++) {
        player.getCards().add(game.getGameDeck().remove(0));
      }

      when(gameService.findById(gameId)).thenReturn(game);

      // When
      List<Card> result = playerService.getPlayerCards(gameId, playerId);

      // Then
      assertThat(result).hasSize(5);
      assertThat(result).isEqualTo(player.getCards());

      verify(gameService, times(1)).findById(gameId);
    }

    @Test
    @DisplayName("Should return empty list when player has no cards")
    void getPlayerCards_whenPlayerHasNoCards_returnsEmptyList() {
      // Given
      String gameId = "game-123";
      Player player = Player.createNew("Alice");
      String playerId = player.getId();
      Game game = Game.createNew();
      game.addPlayer(player);
      when(gameService.findById(gameId)).thenReturn(game);

      // When
      List<Card> result = playerService.getPlayerCards(gameId, playerId);

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should throw PlayerNotFoundException when player does not exist")
    void getPlayerCards_whenPlayerNotFound_throwsException() {
      // Given
      String gameId = "game-123";
      String playerId = "invalid-player-id";
      Game game = Game.createNew();
      when(gameService.findById(gameId)).thenReturn(game);

      // When/Then
      assertThatThrownBy(() -> playerService.getPlayerCards(gameId, playerId))
          .isInstanceOf(PlayerNotFoundException.class)
          .hasMessageContaining(playerId)
          .hasMessageContaining(gameId);
    }
  }

  @Nested
  @DisplayName("getPlayerScores()")
  class GetPlayerScoresTests {

    @Test
    @DisplayName("Should return empty list when game has no players")
    void getPlayerScores_withNoPlayers_returnsEmptyList() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      when(gameService.findById(gameId)).thenReturn(game);

      // When
      List<PlayerScoreResponse> result = playerService.getPlayerScores(gameId);

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return scores for all players sorted by total value descending")
    void getPlayerScores_withMultiplePlayers_returnsSortedScores() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();

      Player alice = Player.createNew("Alice");
      Player bob = Player.createNew("Bob");
      Player charlie = Player.createNew("Charlie");

      game.addPlayer(alice);
      game.addPlayer(bob);
      game.addPlayer(charlie);
      game.addDeck(Deck.createNew());

      // Give different scores to players
      for (int i = 0; i < 5; i++) alice.getCards().add(game.getGameDeck().remove(0)); // 5 cards
      for (int i = 0; i < 3; i++) bob.getCards().add(game.getGameDeck().remove(0)); // 3 cards
      for (int i = 0; i < 2; i++) charlie.getCards().add(game.getGameDeck().remove(0)); // 2 cards

      when(gameService.findById(gameId)).thenReturn(game);

      // When
      List<PlayerScoreResponse> result = playerService.getPlayerScores(gameId);

      // Then
      assertThat(result).hasSize(3);

      // Verify all players are present
      assertThat(result).extracting("name").containsExactlyInAnyOrder("Alice", "Bob", "Charlie");
      assertThat(result).extracting("cardCount").containsExactlyInAnyOrder(5, 3, 2);

      // Verify sorted by totalValue descending (first >= second >= third)
      assertThat(result.get(0).getTotalValue())
          .isGreaterThanOrEqualTo(result.get(1).getTotalValue());
      assertThat(result.get(1).getTotalValue())
          .isGreaterThanOrEqualTo(result.get(2).getTotalValue());
    }

    @Test
    @DisplayName("Should include all player details in response")
    void getPlayerScores_includesAllPlayerDetails() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      Player player = Player.createNew("Alice");
      game.addPlayer(player);
      game.addDeck(Deck.createNew());

      // Add cards to player
      player.getCards().add(game.getGameDeck().remove(0));
      player.getCards().add(game.getGameDeck().remove(0));

      when(gameService.findById(gameId)).thenReturn(game);

      // When
      List<PlayerScoreResponse> result = playerService.getPlayerScores(gameId);

      // Then
      assertThat(result).hasSize(1);
      PlayerScoreResponse response = result.get(0);
      assertThat(response.getPlayerId()).isEqualTo(player.getId());
      assertThat(response.getName()).isEqualTo("Alice");
      assertThat(response.getCardCount()).isEqualTo(2);
      assertThat(response.getTotalValue()).isPositive();
    }
  }
}
