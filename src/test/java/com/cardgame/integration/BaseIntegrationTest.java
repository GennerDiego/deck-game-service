package com.cardgame.integration;

import static org.assertj.core.api.Assertions.*;

import com.cardgame.AbstractIntegrationTest;
import com.cardgame.model.dto.AddPlayerRequest;
import com.cardgame.model.dto.PlayerScoreResponse;
import com.cardgame.model.entity.Card;
import com.cardgame.model.entity.Deck;
import com.cardgame.model.entity.Game;
import java.util.*;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Value;
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
@Tag("integration")
public abstract class BaseIntegrationTest extends AbstractIntegrationTest {

  @Value("${app.security.api-key}")
  protected String apiKey;

  protected HttpHeaders createAuthHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-API-Key", apiKey);
    return headers;
  }

  protected Game createGame() {
    HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
    ResponseEntity<Game> response =
        restTemplate.exchange(baseUrl + "/games", HttpMethod.POST, request, Game.class);
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
    HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
    ResponseEntity<Void> response =
        restTemplate.exchange(baseUrl + "/games/" + gameId, HttpMethod.DELETE, request, Void.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NO_CONTENT);
  }

  /**
   * Create a new deck (standalone)
   *
   * @return created Deck
   */
  protected Deck createDeck() {
    HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
    ResponseEntity<Deck> response =
        restTemplate.exchange(baseUrl + "/decks", HttpMethod.POST, request, Deck.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.CREATED);
    return response.getBody();
  }

  /**
   * Get deck by ID
   *
   * @param deckId deck ID
   * @return Deck
   */
  protected Deck getDeck(String deckId) {
    ResponseEntity<Deck> response =
        restTemplate.getForEntity(baseUrl + "/decks/" + deckId, Deck.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response.getBody();
  }

  /**
   * Get all decks
   *
   * @return list of decks
   */
  protected List<Deck> getAllDecks() {
    ResponseEntity<Deck[]> response = restTemplate.getForEntity(baseUrl + "/decks", Deck[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return Arrays.asList(response.getBody());
  }

  /**
   * Delete a deck
   *
   * @param deckId deck ID
   */
  protected void deleteDeck(String deckId) {
    HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
    ResponseEntity<Void> response =
        restTemplate.exchange(baseUrl + "/decks/" + deckId, HttpMethod.DELETE, request, Void.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NO_CONTENT);
  }

  /**
   * Add an existing deck to a game
   *
   * @param gameId game ID
   * @param deckId deck ID
   */
  protected void addDeckToGame(String gameId, String deckId) {
    HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
    ResponseEntity<Void> response =
        restTemplate.exchange(
            baseUrl + "/games/" + gameId + "/deck/" + deckId, HttpMethod.POST, request, Void.class);
    assertThat(response.getStatusCode())
        .isIn(HttpStatus.OK, HttpStatus.CREATED, HttpStatus.NO_CONTENT);
  }

  /**
   * Helper: Create deck and add to game in one step (for backward compatibility)
   *
   * @param gameId game ID
   */
  protected void createAndAddDeckToGame(String gameId) {
    Deck deck = createDeck();
    addDeckToGame(gameId, deck.getId());
  }

  protected void shuffleDeck(String gameId) {
    HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
    ResponseEntity<Void> response =
        restTemplate.exchange(
            baseUrl + "/games/" + gameId + "/deck/shuffle", HttpMethod.POST, request, Void.class);
    assertThat(response.getStatusCode())
        .isIn(HttpStatus.OK, HttpStatus.CREATED, HttpStatus.NO_CONTENT);
  }

  protected String addPlayer(String gameId, String playerName) {
    AddPlayerRequest requestBody = new AddPlayerRequest(playerName);
    HttpEntity<AddPlayerRequest> request = new HttpEntity<>(requestBody, createAuthHeaders());
    ResponseEntity<Void> response =
        restTemplate.exchange(
            baseUrl + "/games/" + gameId + "/players", HttpMethod.POST, request, Void.class);
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
    HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
    ResponseEntity<Void> response =
        restTemplate.exchange(
            baseUrl + "/games/" + gameId + "/players/" + playerId,
            HttpMethod.DELETE,
            request,
            Void.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NO_CONTENT);
  }

  protected List<Card> dealCards(String gameId, String playerId, int count) {
    HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
    ResponseEntity<Card[]> response =
        restTemplate.exchange(
            baseUrl + "/games/" + gameId + "/players/" + playerId + "/deal?count=" + count,
            HttpMethod.POST,
            request,
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
            baseUrl + "/games/" + gameId + "/deck/suits-count",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response.getBody();
  }

  protected Map<String, Integer> getCardCounts(String gameId) {
    ResponseEntity<Map<String, Integer>> response =
        restTemplate.exchange(
            baseUrl + "/games/" + gameId + "/deck/cards-count",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response.getBody();
  }
}
