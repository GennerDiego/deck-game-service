package com.cardgame.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cardgame.exception.GameNotFoundException;
import com.cardgame.model.entity.Game;
import com.cardgame.repository.GameRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameService - Unit Tests")
class GameServiceTest {

  @Mock private GameRepository gameRepository;

  @InjectMocks private GameService gameService;

  @Nested
  @DisplayName("createGame()")
  class CreateGameTests {

    @Test
    @DisplayName("Should create game with generated ID")
    void createGame_shouldCreateGameWithGeneratedId() {
      // Given
      Game game = Game.createNew();
      when(gameRepository.save(any(Game.class))).thenReturn(game);

      // When
      Game result = gameService.createGame();

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isNotNull();
      assertThat(result.getTotalCardsInDeck()).isZero();
      assertThat(result.getPlayers()).isEmpty();

      verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    @DisplayName("Should save game to repository")
    void createGame_shouldSaveGameToRepository() {
      // Given
      Game savedGame = Game.createNew();
      when(gameRepository.save(any(Game.class))).thenReturn(savedGame);

      // When
      gameService.createGame();

      // Then
      verify(gameRepository, times(1)).save(any(Game.class));
    }
  }

  @Nested
  @DisplayName("findById()")
  class FindByIdTests {

    @Test
    @DisplayName("Should return game when game exists")
    void findById_whenGameExists_returnsGame() {
      // Given
      String gameId = "game-123";
      Game game = Game.createNew();
      when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

      // When
      Game result = gameService.findById(gameId);

      // Then
      assertThat(result).isNotNull();
      assertThat(result).isEqualTo(game);
      verify(gameRepository, times(1)).findById(gameId);
    }

    @Test
    @DisplayName("Should throw GameNotFoundException when game does not exist")
    void findById_whenGameDoesNotExist_throwsException() {
      // Given
      String gameId = "invalid-game-id";
      when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> gameService.findById(gameId))
          .isInstanceOf(GameNotFoundException.class)
          .hasMessageContaining("invalid-game-id");

      verify(gameRepository, times(1)).findById(gameId);
    }
  }

  @Nested
  @DisplayName("deleteGame()")
  class DeleteGameTests {

    @Test
    @DisplayName("Should delete game when game exists")
    void deleteGame_whenGameExists_deletesGame() {
      // Given
      String gameId = "game-123";
      when(gameRepository.existsById(gameId)).thenReturn(true);
      doNothing().when(gameRepository).deleteById(gameId);

      // When
      gameService.deleteGame(gameId);

      // Then
      verify(gameRepository, times(1)).existsById(gameId);
      verify(gameRepository, times(1)).deleteById(gameId);
    }

    @Test
    @DisplayName("Should throw GameNotFoundException when game does not exist")
    void deleteGame_whenGameDoesNotExist_throwsException() {
      // Given
      String gameId = "invalid-game-id";
      when(gameRepository.existsById(gameId)).thenReturn(false);

      // When/Then
      assertThatThrownBy(() -> gameService.deleteGame(gameId))
          .isInstanceOf(GameNotFoundException.class)
          .hasMessageContaining("invalid-game-id");

      verify(gameRepository, times(1)).existsById(gameId);
      verify(gameRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should not call deleteById when game does not exist")
    void deleteGame_whenGameDoesNotExist_doesNotCallDelete() {
      // Given
      String gameId = "invalid-game-id";
      when(gameRepository.existsById(gameId)).thenReturn(false);

      // When/Then
      try {
        gameService.deleteGame(gameId);
      } catch (GameNotFoundException e) {
        // Expected
      }

      verify(gameRepository, never()).deleteById(gameId);
    }
  }
}
