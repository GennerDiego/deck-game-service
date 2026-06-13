package com.cardgame.integration;

import static org.assertj.core.api.Assertions.*;

import com.cardgame.AbstractIntegrationTest;
import com.cardgame.model.dto.AddPlayerRequest;
import com.cardgame.model.dto.PlayerScoreResponse;
import com.cardgame.model.entity.Card;
import com.cardgame.model.entity.Game;
import java.util.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

/**
 * Base class for integration tests with common helper methods.
 *
 * <p>Provides reusable methods for:
 *
 * <ul>
 *   <li>Creating and managing games
 *   <li>Adding decks and players
 *   <li>Dealing cards and shuffling
 *   <li>Querying game state (scores, counts, etc.)
 * </ul>
 */
public abstract class BaseIntegrationTest extends AbstractIntegrationTest {

  // ==================== GAME OPERATIONS ====================

  protected Game createGame() {
    ResponseEntity<Game> response =
        restTemplate.postForEntity(baseUrl + "/games", null, Game.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.CREATED);
    return response.getBody();
  }

  protected Game getGame(String gameId) {
    ResponseEntity<Game> response =
        restTemplate.getForEntity(baseUrl + "/games/" + gameId, Game.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response.getBody();
  }

  protected void deleteGame(String gameId) {
    ResponseEntity<Void> response =
        restTemplate.exchange(baseUrl + "/games/" + gameId, HttpMethod.DELETE, null, Void.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NO_CONTENT);
  }

  // ==================== DECK OPERATIONS ====================

  protected void addDeckToGame(String gameId) {
    ResponseEntity<Void> response =
        restTemplate.postForEntity(baseUrl + "/games/" + gameId + "/decks", null, Void.class);
    assertThat(response.getStatusCode())
        .isIn(HttpStatus.OK, HttpStatus.CREATED, HttpStatus.NO_CONTENT);
  }

  protected void shuffleDeck(String gameId) {
    ResponseEntity<Void> response =
        restTemplate.postForEntity(
            baseUrl + "/games/" + gameId + "/decks/shuffle", null, Void.class);
    assertThat(response.getStatusCode())
        .isIn(HttpStatus.OK, HttpStatus.CREATED, HttpStatus.NO_CONTENT);
  }

  // ==================== PLAYER OPERATIONS ====================

  protected String addPlayer(String gameId, String playerName) {
    AddPlayerRequest request = new AddPlayerRequest(playerName);
    ResponseEntity<Void> response =
        restTemplate.postForEntity(baseUrl + "/games/" + gameId + "/players", request, Void.class);
    assertThat(response.getStatusCode())
        .isIn(HttpStatus.OK, HttpStatus.CREATED, HttpStatus.NO_CONTENT);

    // Get game to retrieve player ID
    Game game = getGame(gameId);
    return game.getPlayers().stream()
        .filter(p -> p.getName().equals(playerName))
        .findFirst()
        .orElseThrow()
        .getId();
  }

  protected void removePlayer(String gameId, String playerId) {
    ResponseEntity<Void> response =
        restTemplate.exchange(
            baseUrl + "/games/" + gameId + "/players/" + playerId,
            HttpMethod.DELETE,
            null,
            Void.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NO_CONTENT);
  }

  // ==================== CARD OPERATIONS ====================

  protected List<Card> dealCards(String gameId, String playerId, int count) {
    ResponseEntity<Card[]> response =
        restTemplate.exchange(
            baseUrl + "/games/" + gameId + "/players/" + playerId + "/deal?count=" + count,
            HttpMethod.POST,
            null,
            Card[].class);
    assertThat(response.getStatusCode())
        .isIn(HttpStatus.OK, HttpStatus.CREATED, HttpStatus.NO_CONTENT);
    return Arrays.asList(response.getBody());
  }

  protected List<Card> getPlayerCards(String gameId, String playerId) {
    ResponseEntity<Card[]> response =
        restTemplate.getForEntity(
            baseUrl + "/games/" + gameId + "/players/" + playerId + "/cards", Card[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return Arrays.asList(response.getBody());
  }

  // ==================== QUERY OPERATIONS ====================

  protected List<PlayerScoreResponse> getPlayerScores(String gameId) {
    ResponseEntity<PlayerScoreResponse[]> response =
        restTemplate.getForEntity(
            baseUrl + "/games/" + gameId + "/players/scores", PlayerScoreResponse[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return Arrays.asList(response.getBody());
  }

  protected Map<String, Integer> getSuitCounts(String gameId) {
    ResponseEntity<Map<String, Integer>> response =
        restTemplate.exchange(
            baseUrl + "/games/" + gameId + "/decks/suits-count",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response.getBody();
  }

  protected Map<String, Integer> getCardCounts(String gameId) {
    ResponseEntity<Map<String, Integer>> response =
        restTemplate.exchange(
            baseUrl + "/games/" + gameId + "/decks/cards-count",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response.getBody();
  }
}
