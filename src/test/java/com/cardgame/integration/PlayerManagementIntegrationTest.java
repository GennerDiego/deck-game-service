package com.cardgame.integration;

import static org.assertj.core.api.Assertions.*;

import com.cardgame.model.dto.AddPlayerRequest;
import com.cardgame.model.entity.Game;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

@DisplayName("Player Management - Add and Remove Players")
class PlayerManagementIntegrationTest extends BaseIntegrationTest {

  @Test
  @DisplayName("Should add first player to game successfully")
  void addPlayer_whenGameHasNoPlayers_addsPlayerSuccessfully() {
    // Given
    Game game = createGame();

    // When
    addPlayer(game.getId(), "Alice");

    // Then
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getPlayers()).hasSize(1);
    assertThat(updatedGame.getPlayers().get(0).getName()).isEqualTo("Alice");
  }

  @Test
  @DisplayName("Should add multiple players to game")
  void addPlayer_whenGameHasPlayers_addsAnotherPlayer() {
    // Given
    Game game = createGame();
    addPlayer(game.getId(), "Alice");
    addPlayer(game.getId(), "Bob");

    // When
    addPlayer(game.getId(), "Charlie");

    // Then
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getPlayers()).hasSize(3);

    List<String> playerNames =
        updatedGame.getPlayers().stream().map(p -> p.getName()).collect(Collectors.toList());
    assertThat(playerNames).containsExactlyInAnyOrder("Alice", "Bob", "Charlie");
  }

  @Test
  @DisplayName("Should remove player and move cards to discard pile")
  void removePlayer_whenPlayerExists_removesPlayerAndMovesCardsToDiscard() {
    // Given
    Game game = createGame();
    addDeckToGame(game.getId());
    String playerId = addPlayer(game.getId(), "Alice");

    // Deal 5 cards to player
    dealCards(game.getId(), playerId, 5);

    // When
    removePlayer(game.getId(), playerId);

    // Then
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getPlayers()).isEmpty();
    //    assertThat(updatedGame.getDiscardedCards()).hasSize(5);
  }

  @Test
  @DisplayName("Should return 404 when removing non-existent player")
  void removePlayer_whenPlayerDoesNotExist_returns404() {
    // Given
    Game game = createGame();
    String invalidPlayerId = "invalid-player-id";

    // When
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            baseUrl + "/games/" + game.getId() + "/players/" + invalidPlayerId,
            HttpMethod.DELETE,
            null,
            new ParameterizedTypeReference<>() {});

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().get("message").toString()).contains(invalidPlayerId);
  }

  @Test
  @DisplayName("Should return 404 when adding player to non-existent game")
  void addPlayer_whenGameDoesNotExist_returns404() {
    // Given
    String invalidGameId = "invalid-game-id";
    AddPlayerRequest request = new AddPlayerRequest("Alice");

    // When
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            baseUrl + "/games/" + invalidGameId + "/players",
            HttpMethod.POST,
            new HttpEntity<>(request),
            new ParameterizedTypeReference<>() {});

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
