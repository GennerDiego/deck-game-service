package com.cardgame.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.cardgame.exception.GameNotFoundException;
import com.cardgame.exception.PlayerNotFoundException;
import com.cardgame.interceptor.ApiKeyInterceptor;
import com.cardgame.model.dto.PlayerScoreResponse;
import com.cardgame.model.entity.Card;
import com.cardgame.model.entity.Rank;
import com.cardgame.model.entity.Suit;
import com.cardgame.service.PlayerService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlayerController.class)
@DisplayName("PlayerController - Unit Tests")
class PlayerControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PlayerService playerService;

  @MockBean private ApiKeyInterceptor apiKeyInterceptor;

  @Nested
  @DisplayName("POST /games/{gameId}/players - Add Player")
  class AddPlayerTests {

    @Test
    @DisplayName("Should return 401 when API key is missing")
    void addPlayer_withoutApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(
              new com.cardgame.exception.UnauthorizedException(
                  "API Key is required. Provide X-API-Key header."));

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/players", "game-123")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\": \"Alice\"}"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.status").value(401))
          .andExpect(jsonPath("$.message").value("API Key is required. Provide X-API-Key header."));

      verify(playerService, never()).addPlayer(any(), any());
    }

    @Test
    @DisplayName("Should return 401 when API key is invalid")
    void addPlayer_withInvalidApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(new com.cardgame.exception.UnauthorizedException("Invalid API Key."));

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/players", "game-123")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\": \"Alice\"}")
                  .header("X-API-Key", "invalid-key"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("Invalid API Key."));

      verify(playerService, never()).addPlayer(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when player name is blank")
    void addPlayer_withBlankName_returnsBadRequest() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/players", "game-123")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\": \"\"}")
                  .header("X-API-Key", "valid-key"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.error").value("Bad Request"))
          .andExpect(jsonPath("$.message").value("name: Player name is required"));

      verify(playerService, never()).addPlayer(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when player name is null")
    void addPlayer_withNullName_returnsBadRequest() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/players", "game-123")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{}")
                  .header("X-API-Key", "valid-key"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("name: Player name is required"));
    }

    @Test
    @DisplayName("Should return 404 when game does not exist")
    void addPlayer_whenGameNotFound_returns404() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      String gameId = "invalid-game-id";
      doThrow(new GameNotFoundException(gameId)).when(playerService).addPlayer(eq(gameId), any());

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/players", gameId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\": \"Alice\"}")
                  .header("X-API-Key", "valid-key"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Game with ID 'invalid-game-id' not found"));
    }

    @Test
    @DisplayName("Should return 204 when player is added successfully")
    void addPlayer_withValidData_returnsNoContent() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
      doNothing().when(playerService).addPlayer(eq("game-123"), any());

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/players", "game-123")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\": \"Alice\"}")
                  .header("X-API-Key", "valid-key"))
          .andExpect(status().isNoContent());

      verify(playerService, times(1)).addPlayer(eq("game-123"), any());
    }
  }

  @Nested
  @DisplayName("DELETE /games/{gameId}/players/{playerId} - Remove Player")
  class RemovePlayerTests {

    @Test
    @DisplayName("Should return 401 when API key is missing")
    void removePlayer_withoutApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(
              new com.cardgame.exception.UnauthorizedException(
                  "API Key is required. Provide X-API-Key header."));

      // When/Then
      mockMvc
          .perform(delete("/games/{gameId}/players/{playerId}", "game-123", "player-456"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("API Key is required. Provide X-API-Key header."));

      verify(playerService, never()).removePlayer(any(), any());
    }

    @Test
    @DisplayName("Should return 401 when API key is invalid")
    void removePlayer_withInvalidApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(new com.cardgame.exception.UnauthorizedException("Invalid API Key."));

      // When/Then
      mockMvc
          .perform(
              delete("/games/{gameId}/players/{playerId}", "game-123", "player-456")
                  .header("X-API-Key", "invalid-key"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("Invalid API Key."));

      verify(playerService, never()).removePlayer(any(), any());
    }

    @Test
    @DisplayName("Should return 404 when player does not exist")
    void removePlayer_whenPlayerNotFound_returns404() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      String gameId = "game-123";
      String playerId = "invalid-player-id";
      doThrow(new PlayerNotFoundException(playerId, gameId))
          .when(playerService)
          .removePlayer(gameId, playerId);

      // When/Then
      mockMvc
          .perform(
              delete("/games/{gameId}/players/{playerId}", gameId, playerId)
                  .header("X-API-Key", "valid-key"))
          .andExpect(status().isNotFound())
          .andExpect(
              jsonPath("$.message")
                  .value("Player with ID 'invalid-player-id' not found in game 'game-123'"));
    }

    @Test
    @DisplayName("Should return 204 when player is removed successfully")
    void removePlayer_withValidIds_returnsNoContent() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
      doNothing().when(playerService).removePlayer("game-123", "player-456");

      // When/Then
      mockMvc
          .perform(
              delete("/games/{gameId}/players/{playerId}", "game-123", "player-456")
                  .header("X-API-Key", "valid-key"))
          .andExpect(status().isNoContent());

      verify(playerService, times(1)).removePlayer("game-123", "player-456");
    }
  }

  @Nested
  @DisplayName("POST /games/{gameId}/players/{playerId}/deal - Deal Cards")
  class DealCardsTests {

    @Test
    @DisplayName("Should return 401 when API key is missing")
    void dealCards_withoutApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(
              new com.cardgame.exception.UnauthorizedException(
                  "API Key is required. Provide X-API-Key header."));

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/players/{playerId}/deal", "game-123", "player-456")
                  .param("count", "2"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("API Key is required. Provide X-API-Key header."));

      verify(playerService, never()).dealCards(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Should return 401 when API key is invalid")
    void dealCards_withInvalidApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(new com.cardgame.exception.UnauthorizedException("Invalid API Key."));

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/players/{playerId}/deal", "game-123", "player-456")
                  .param("count", "2")
                  .header("X-API-Key", "invalid-key"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("Invalid API Key."));

      verify(playerService, never()).dealCards(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Should return 400 when count is zero")
    void dealCards_withZeroCount_returnsBadRequest() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/players/{playerId}/deal", "game-123", "player-456")
                  .param("count", "0")
                  .header("X-API-Key", "valid-key"))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value("dealCardsToPlayer.count: Count must be positive"));

      verify(playerService, never()).dealCards(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Should return 400 when count is negative")
    void dealCards_withNegativeCount_returnsBadRequest() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/players/{playerId}/deal", "game-123", "player-456")
                  .param("count", "-5")
                  .header("X-API-Key", "valid-key"))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value("dealCardsToPlayer.count: Count must be positive"));
    }

    @Test
    @DisplayName("Should return 200 with dealt cards when count is valid")
    void dealCards_withValidCount_returnsOk() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      List<Card> dealtCards =
          List.of(new Card(Suit.HEARTS, Rank.ACE), new Card(Suit.SPADES, Rank.KING));

      when(playerService.dealCards("game-123", "player-456", 2)).thenReturn(dealtCards);

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/players/{playerId}/deal", "game-123", "player-456")
                  .param("count", "2")
                  .header("X-API-Key", "valid-key"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].suit").value("HEARTS"))
          .andExpect(jsonPath("$[0].rank").value("ACE"))
          .andExpect(jsonPath("$[1].suit").value("SPADES"))
          .andExpect(jsonPath("$[1].rank").value("KING"));

      verify(playerService, times(1)).dealCards("game-123", "player-456", 2);
    }

    @Test
    @DisplayName("Should use default count of 1 when count parameter is omitted")
    void dealCards_withoutCountParam_usesDefaultValue() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      List<Card> dealtCards = List.of(new Card(Suit.DIAMONDS, Rank.QUEEN));
      when(playerService.dealCards("game-123", "player-456", 1)).thenReturn(dealtCards);

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/players/{playerId}/deal", "game-123", "player-456")
                  .header("X-API-Key", "valid-key"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(1));

      verify(playerService, times(1)).dealCards("game-123", "player-456", 1);
    }
  }

  @Nested
  @DisplayName("GET /games/{gameId}/players/{playerId}/cards - Get Player Cards")
  class GetPlayerCardsTests {

    @Test
    @DisplayName("Should return 200 with empty list when player has no cards")
    void getPlayerCards_whenPlayerHasNoCards_returnsEmptyList() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
      when(playerService.getPlayerCards("game-123", "player-456")).thenReturn(List.of());

      // When/Then
      mockMvc
          .perform(get("/games/{gameId}/players/{playerId}/cards", "game-123", "player-456"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should return 200 with cards when player has cards")
    void getPlayerCards_whenPlayerHasCards_returnsCards() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      List<Card> playerCards =
          List.of(
              new Card(Suit.HEARTS, Rank.ACE),
              new Card(Suit.CLUBS, Rank.TEN),
              new Card(Suit.DIAMONDS, Rank.FIVE));

      when(playerService.getPlayerCards("game-123", "player-456")).thenReturn(playerCards);

      // When/Then
      mockMvc
          .perform(get("/games/{gameId}/players/{playerId}/cards", "game-123", "player-456"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(3))
          .andExpect(jsonPath("$[0].suit").value("HEARTS"))
          .andExpect(jsonPath("$[0].rank").value("ACE"));
    }

    @Test
    @DisplayName("Should return 404 when player does not exist")
    void getPlayerCards_whenPlayerNotFound_returns404() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      when(playerService.getPlayerCards("game-123", "invalid-player"))
          .thenThrow(new PlayerNotFoundException("invalid-player", "game-123"));

      // When/Then
      mockMvc
          .perform(get("/games/{gameId}/players/{playerId}/cards", "game-123", "invalid-player"))
          .andExpect(status().isNotFound())
          .andExpect(
              jsonPath("$.message")
                  .value("Player with ID 'invalid-player' not found in game 'game-123'"));
    }
  }

  @Nested
  @DisplayName("GET /games/{gameId}/players/scores - Get Player Scores")
  class GetPlayerScoresTests {

    @Test
    @DisplayName("Should return 200 with empty list when game has no players")
    void getPlayerScores_whenNoPlayers_returnsEmptyList() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
      when(playerService.getPlayerScores("game-123")).thenReturn(List.of());

      // When/Then
      mockMvc
          .perform(get("/games/{gameId}/players/scores", "game-123"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should return 200 with scores sorted by score descending")
    void getPlayerScores_withMultiplePlayers_returnsSortedScores() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      List<PlayerScoreResponse> scores =
          List.of(
              new PlayerScoreResponse("player-1", "Alice", 25, 5),
              new PlayerScoreResponse("player-2", "Bob", 18, 3),
              new PlayerScoreResponse("player-3", "Charlie", 10, 2));

      when(playerService.getPlayerScores("game-123")).thenReturn(scores);

      // When/Then
      mockMvc
          .perform(get("/games/{gameId}/players/scores", "game-123"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(3))
          .andExpect(jsonPath("$[0].playerId").value("player-1"))
          .andExpect(jsonPath("$[0].name").value("Alice"))
          .andExpect(jsonPath("$[0].totalValue").value(25))
          .andExpect(jsonPath("$[0].cardCount").value(5))
          .andExpect(jsonPath("$[1].totalValue").value(18))
          .andExpect(jsonPath("$[2].totalValue").value(10));
    }
  }
}
