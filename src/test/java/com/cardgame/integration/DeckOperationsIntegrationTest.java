package com.cardgame.integration;

import static org.assertj.core.api.Assertions.*;

import com.cardgame.model.entity.Deck;
import com.cardgame.model.entity.Game;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("Deck Operations - Integration Tests")
public class DeckOperationsIntegrationTest extends BaseIntegrationTest {

  @Test
  @DisplayName("Should create a new deck with 52 cards")
  void createDeck_shouldReturnStandardDeck() {
    // When
    Deck deck = createDeck();

    // Then
    assertThat(deck).isNotNull();
    assertThat(deck.getId()).isNotNull();
    assertThat(deck.getCards()).hasSize(52);
    assertThat(deck.getCreatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should get deck by ID")
  void getDeck_whenDeckExists_returnsDeck() {
    // Given
    Deck createdDeck = createDeck();

    // When
    Deck fetchedDeck = getDeck(createdDeck.getId());

    // Then
    assertThat(fetchedDeck).isNotNull();
    assertThat(fetchedDeck.getId()).isEqualTo(createdDeck.getId());
    assertThat(fetchedDeck.getCards()).hasSize(52);
  }

  @Test
  @DisplayName("Should return 404 when deck does not exist")
  void getDeck_whenDeckNotFound_returns404() {
    // When
    ResponseEntity<String> response =
        restTemplate.getForEntity(baseUrl + "/decks/invalid-deck-id", String.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).contains("Deck with ID 'invalid-deck-id' not found");
  }

  @Test
  @DisplayName("Should get all decks")
  void getAllDecks_whenDecksExist_returnsAllDecks() {
    // Given
    Deck deck1 = createDeck();
    Deck deck2 = createDeck();

    // When
    List<Deck> decks = getAllDecks();

    // Then
    assertThat(decks).isNotEmpty();
    assertThat(decks).extracting(Deck::getId).contains(deck1.getId(), deck2.getId());
  }

  @Test
  @DisplayName("Should delete deck when not in use by any game")
  void deleteDeck_whenNotInUse_deletesSuccessfully() {
    // Given
    Deck deck = createDeck();

    // When
    deleteDeck(deck.getId());

    // Then - Verify deck is deleted
    ResponseEntity<String> response =
        restTemplate.getForEntity(baseUrl + "/decks/" + deck.getId(), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("Should return 409 when trying to delete deck in use by a game")
  void deleteDeck_whenInUseByGame_returns409() {
    // Given
    Game game = createGame();
    Deck deck = createDeck();
    addDeckToGame(game.getId(), deck.getId());

    // When
    HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl + "/decks/" + deck.getId(), HttpMethod.DELETE, request, String.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).contains("in use by a game").contains("cannot be deleted");
  }

  @Test
  @DisplayName("Should add existing deck to game")
  void addDeckToGame_withExistingDeck_addsSuccessfully() {
    // Given
    Game game = createGame();
    Deck deck = createDeck();

    // When
    addDeckToGame(game.getId(), deck.getId());

    // Then
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(52);
    assertThat(updatedGame.getTotalCardsInDeck()).isEqualTo(52);
    assertThat(updatedGame.getDeckIdsInUse()).contains(deck.getId());
  }

  @Test
  @DisplayName("Should add multiple decks to game (shoe)")
  void addDeckToGame_withMultipleDecks_createsShoe() {
    // Given
    Game game = createGame();
    Deck deck1 = createDeck();
    Deck deck2 = createDeck();
    Deck deck3 = createDeck();

    // When
    addDeckToGame(game.getId(), deck1.getId());
    addDeckToGame(game.getId(), deck2.getId());
    addDeckToGame(game.getId(), deck3.getId());

    // Then
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(156); // 3 × 52
    assertThat(updatedGame.getTotalCardsInDeck()).isEqualTo(156);
    assertThat(updatedGame.getDeckIdsInUse())
        .containsExactlyInAnyOrder(deck1.getId(), deck2.getId(), deck3.getId());
  }

  @Test
  @DisplayName("Should return 404 when adding non-existent deck to game")
  void addDeckToGame_whenDeckNotFound_returns404() {
    // Given
    Game game = createGame();

    // When
    HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl + "/games/" + game.getId() + "/deck/invalid-deck-id",
            HttpMethod.POST,
            request,
            String.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).contains("Deck with ID 'invalid-deck-id' not found");
  }

  @Test
  @DisplayName("Should return 404 when adding deck to non-existent game")
  void addDeckToGame_whenGameNotFound_returns404() {
    // Given
    Deck deck = createDeck();

    // When
    HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl + "/games/invalid-game-id/deck/" + deck.getId(),
            HttpMethod.POST,
            request,
            String.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).contains("Game with ID 'invalid-game-id' not found");
  }

  @Test
  @DisplayName("Should return 409 when adding same deck twice to same game")
  void addDeckToGame_whenDeckAlreadyAdded_returns409() {
    // Given
    Game game = createGame();
    Deck deck = createDeck();

    // Add deck first time (should succeed)
    addDeckToGame(game.getId(), deck.getId());

    // When - Try to add same deck again
    HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl + "/games/" + game.getId() + "/deck/" + deck.getId(),
            HttpMethod.POST,
            request,
            String.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).contains("already added");
    assertThat(response.getBody()).contains(deck.getId());

    // Verify game still has only 52 cards (one deck)
    Game updatedGame = getGame(game.getId());
    assertThat(updatedGame.getCardsRemaining()).isEqualTo(52);
    assertThat(updatedGame.getTotalDecksAdded()).isEqualTo(1);
  }

  @Test
  @DisplayName(
      "[COMPLIANCE] Should allow deck deletion after game is deleted (deck no longer in use)")
  void deleteDeck_afterGameDeleted_allowsDeletion() {
    // Given
    Game game = createGame();
    Deck deck = createDeck();
    addDeckToGame(game.getId(), deck.getId());

    // Verify deck cannot be deleted while game exists
    HttpEntity<Void> deleteRequest = new HttpEntity<>(createAuthHeaders());
    ResponseEntity<String> conflictResponse =
        restTemplate.exchange(
            baseUrl + "/decks/" + deck.getId(), HttpMethod.DELETE, deleteRequest, String.class);
    assertThat(conflictResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

    // When - Delete the game
    deleteGame(game.getId());

    // Then - Now deck can be deleted
    deleteDeck(deck.getId());

    // Verify deck is deleted
    ResponseEntity<String> notFoundResponse =
        restTemplate.getForEntity(baseUrl + "/decks/" + deck.getId(), String.class);
    assertThat(notFoundResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
