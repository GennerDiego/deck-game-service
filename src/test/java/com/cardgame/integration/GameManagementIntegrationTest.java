package com.cardgame.integration;

import static org.assertj.core.api.Assertions.*;

import com.cardgame.model.entity.Game;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

@DisplayName("Game Management - Create, Get, Delete")
class GameManagementIntegrationTest extends BaseIntegrationTest {

  @Test
  @DisplayName("Should create a game with valid ID and initial state")
  void createGame_whenCalled_returnsGameWithId() {
    // When
    Game game = createGame();

    // Then
    assertThat(game.getId()).isNotNull().matches("[0-9a-fA-F-]{36}"); // UUID format
    assertThat(game.getTotalCardsInDeck()).isEqualTo(0);
    assertThat(game.getCardsRemaining()).isEqualTo(0);
    assertThat(game.getPlayersList()).isEmpty();
  }

  @Test
  @DisplayName("Should get game details when game exists")
  void getGameDetails_whenGameExists_returnsFullGameData() {
    // Given
    Game createdGame = createGame();

    // When
    Game fetchedGame = getGame(createdGame.getId());

    // Then
    assertThat(fetchedGame.getId()).isEqualTo(createdGame.getId());
  }

  @Test
  @DisplayName("Should return 404 when getting non-existent game")
  void getGameDetails_whenGameDoesNotExist_returns404() {
    // Given
    String invalidGameId = "invalid-game-id-12345";

    // When
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            baseUrl + "/games/" + invalidGameId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("status")).isEqualTo(404);
    assertThat(response.getBody().get("message").toString()).contains(invalidGameId);
  }

  @Test
  @DisplayName("Should delete game successfully and return 404 on subsequent GET")
  void deleteGame_whenGameExists_removesGameFromRedis() {
    // Given
    Game game = createGame();

    // When
    deleteGame(game.getId());

    // Then - Verify game no longer exists
    ResponseEntity<Map<String, Object>> getResponse =
        restTemplate.exchange(
            baseUrl + "/games/" + game.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("Should return 404 when deleting non-existent game")
  void deleteGame_whenGameDoesNotExist_returns404() {
    // Given
    String invalidGameId = "invalid-game-id";

    // When
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            baseUrl + "/games/" + invalidGameId,
            HttpMethod.DELETE,
            null,
            new ParameterizedTypeReference<>() {});

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
