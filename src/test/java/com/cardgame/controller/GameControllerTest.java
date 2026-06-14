package com.cardgame.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.cardgame.exception.GameNotFoundException;
import com.cardgame.exception.UnauthorizedException;
import com.cardgame.interceptor.ApiKeyInterceptor;
import com.cardgame.model.entity.Game;
import com.cardgame.service.GameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GameController.class)
@DisplayName("GameController - Unit Tests")
class GameControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GameService gameService;

  @MockBean private ApiKeyInterceptor apiKeyInterceptor;

  @Nested
  @DisplayName("POST /games - Create Game")
  class CreateGameTests {

    @Test
    @DisplayName("Should return 401 when API key is missing")
    void createGame_withoutApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(new UnauthorizedException("API Key is required. Provide X-API-Key header."));

      // When/Then
      mockMvc
          .perform(post("/games"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.status").value(401))
          .andExpect(jsonPath("$.error").value("Unauthorized"))
          .andExpect(jsonPath("$.message").value("API Key is required. Provide X-API-Key header."));

      verify(gameService, never()).createGame();
    }

    @Test
    @DisplayName("Should return 401 when API key is invalid")
    void createGame_withInvalidApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(new UnauthorizedException("Invalid API Key."));

      // When/Then
      mockMvc
          .perform(post("/games").header("X-API-Key", "invalid-key"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.status").value(401))
          .andExpect(jsonPath("$.message").value("Invalid API Key."));

      verify(gameService, never()).createGame();
    }

    @Test
    @DisplayName("Should return 201 with game when API key is valid")
    void createGame_withValidApiKey_returnsCreated() throws Exception {
      // Given - Valid API key passes interceptor
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      Game game = Game.createNew();
      when(gameService.createGame()).thenReturn(game);

      // When/Then
      mockMvc
          .perform(post("/games").header("X-API-Key", "valid-key"))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(game.getId()))
          .andExpect(jsonPath("$.totalCardsInDeck").value(0))
          .andExpect(jsonPath("$.cardsRemaining").value(0))
          .andExpect(jsonPath("$.players").isEmpty());

      verify(gameService, times(1)).createGame();
    }
  }

  @Nested
  @DisplayName("GET /games/{gameId} - Get Game")
  class GetGameTests {

    @Test
    @DisplayName("Should return 200 with game when game exists (no API key required)")
    void getGame_whenGameExists_returnsOk() throws Exception {
      // Given - No API key required for GET
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      String gameId = "game-123";
      Game game = Game.createNew();
      when(gameService.findById(gameId)).thenReturn(game);

      // When/Then
      mockMvc
          .perform(get("/games/{gameId}", gameId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(game.getId()));

      verify(gameService, times(1)).findById(gameId);
    }

    @Test
    @DisplayName("Should return 404 when game does not exist")
    void getGame_whenGameNotFound_returns404() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      String gameId = "invalid-game-id";
      when(gameService.findById(gameId)).thenThrow(new GameNotFoundException(gameId));

      // When/Then
      mockMvc
          .perform(get("/games/{gameId}", gameId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.status").value(404))
          .andExpect(jsonPath("$.error").value("Not Found"))
          .andExpect(jsonPath("$.message").value("Game with ID 'invalid-game-id' not found"));
    }
  }

  @Nested
  @DisplayName("DELETE /games/{gameId} - Delete Game")
  class DeleteGameTests {

    @Test
    @DisplayName("Should return 401 when API key is missing")
    void deleteGame_withoutApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(new UnauthorizedException("API Key is required. Provide X-API-Key header."));

      // When/Then
      mockMvc
          .perform(delete("/games/{gameId}", "game-123"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("API Key is required. Provide X-API-Key header."));

      verify(gameService, never()).deleteGame(any());
    }

    @Test
    @DisplayName("Should return 204 when game is deleted successfully")
    void deleteGame_withValidApiKey_returnsNoContent() throws Exception {
      // Given - Valid API key
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      String gameId = "game-123";
      doNothing().when(gameService).deleteGame(gameId);

      // When/Then
      mockMvc
          .perform(delete("/games/{gameId}", gameId).header("X-API-Key", "valid-key"))
          .andExpect(status().isNoContent());

      verify(gameService, times(1)).deleteGame(gameId);
    }

    @Test
    @DisplayName("Should return 404 when game to delete does not exist")
    void deleteGame_whenGameNotFound_returns404() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      String gameId = "invalid-game-id";
      doThrow(new GameNotFoundException(gameId)).when(gameService).deleteGame(gameId);

      // When/Then
      mockMvc
          .perform(delete("/games/{gameId}", gameId).header("X-API-Key", "valid-key"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Game with ID 'invalid-game-id' not found"));
    }
  }
}
