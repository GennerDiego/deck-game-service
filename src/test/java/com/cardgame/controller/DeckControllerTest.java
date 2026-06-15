package com.cardgame.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.cardgame.exception.DeckInUseException;
import com.cardgame.exception.DeckNotFoundException;
import com.cardgame.exception.GameNotFoundException;
import com.cardgame.interceptor.ApiKeyInterceptor;
import com.cardgame.model.entity.Card;
import com.cardgame.model.entity.Deck;
import com.cardgame.model.entity.Rank;
import com.cardgame.model.entity.Suit;
import com.cardgame.repository.DeckRepository;
import com.cardgame.service.DeckService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DeckController.class)
@DisplayName("DeckController - Unit Tests")
class DeckControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private DeckService deckService;

  @MockBean private DeckRepository deckRepository;

  @MockBean private ApiKeyInterceptor apiKeyInterceptor;

  @Nested
  @DisplayName("POST /decks - Create Deck")
  class CreateDeckTests {

    @Test
    @DisplayName("Should return 401 when API key is missing")
    void createDeck_withoutApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(
              new com.cardgame.exception.UnauthorizedException(
                  "API Key is required. Provide X-API-Key header."));

      // When/Then
      mockMvc
          .perform(post("/decks"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("API Key is required. Provide X-API-Key header."));

      verify(deckService, never()).createDeck();
    }

    @Test
    @DisplayName("Should return 201 with created deck when API key is valid")
    void createDeck_withValidApiKey_returnsCreatedDeck() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      List<Card> cards = new ArrayList<>();
      cards.add(Card.builder().suit(Suit.HEARTS).rank(Rank.ACE).build());

      Deck deck = Deck.builder().id("deck-123").cards(cards).build();
      when(deckService.createDeck()).thenReturn(deck);

      // When/Then
      mockMvc
          .perform(post("/decks").header("X-API-Key", "valid-key"))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value("deck-123"))
          .andExpect(jsonPath("$.cards").isArray());

      verify(deckService, times(1)).createDeck();
    }
  }

  @Nested
  @DisplayName("GET /decks - Get All Decks")
  class GetAllDecksTests {

    @Test
    @DisplayName("Should return 200 with empty list when no decks exist")
    void getAllDecks_whenNoDecks_returnsEmptyList() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
      when(deckService.getAllDecks()).thenReturn(List.of());

      // When/Then
      mockMvc
          .perform(get("/decks"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should return 200 with list of decks")
    void getAllDecks_whenDecksExist_returnsList() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      Deck deck1 = Deck.builder().id("deck-1").cards(new ArrayList<>()).build();
      Deck deck2 = Deck.builder().id("deck-2").cards(new ArrayList<>()).build();

      when(deckService.getAllDecks()).thenReturn(List.of(deck1, deck2));

      // When/Then
      mockMvc
          .perform(get("/decks"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].id").value("deck-1"))
          .andExpect(jsonPath("$[1].id").value("deck-2"));
    }
  }

  @Nested
  @DisplayName("GET /decks/{deckId} - Get Deck by ID")
  class GetDeckByIdTests {

    @Test
    @DisplayName("Should return 404 when deck does not exist")
    void getDeck_whenDeckNotFound_returns404() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      String deckId = "invalid-deck-id";
      when(deckService.findById(deckId)).thenThrow(new DeckNotFoundException(deckId));

      // When/Then
      mockMvc
          .perform(get("/decks/{deckId}", deckId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Deck with ID 'invalid-deck-id' not found"));
    }

    @Test
    @DisplayName("Should return 200 with deck when deck exists")
    void getDeck_whenDeckExists_returnsDeck() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      List<Card> cards = new ArrayList<>();
      cards.add(Card.builder().suit(Suit.HEARTS).rank(Rank.ACE).build());

      Deck deck = Deck.builder().id("deck-123").cards(cards).build();
      when(deckService.findById("deck-123")).thenReturn(deck);

      // When/Then
      mockMvc
          .perform(get("/decks/{deckId}", "deck-123"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value("deck-123"))
          .andExpect(jsonPath("$.cards").isArray())
          .andExpect(jsonPath("$.cards.length()").value(1));
    }
  }

  @Nested
  @DisplayName("DELETE /decks/{deckId} - Delete Deck")
  class DeleteDeckTests {

    @Test
    @DisplayName("Should return 401 when API key is missing")
    void deleteDeck_withoutApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(
              new com.cardgame.exception.UnauthorizedException(
                  "API Key is required. Provide X-API-Key header."));

      // When/Then
      mockMvc
          .perform(delete("/decks/{deckId}", "deck-123"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("API Key is required. Provide X-API-Key header."));

      verify(deckService, never()).deleteDeck(anyString());
    }

    @Test
    @DisplayName("Should return 404 when deck does not exist")
    void deleteDeck_whenDeckNotFound_returns404() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      String deckId = "invalid-deck-id";
      doThrow(new DeckNotFoundException(deckId)).when(deckService).deleteDeck(deckId);

      // When/Then
      mockMvc
          .perform(delete("/decks/{deckId}", deckId).header("X-API-Key", "valid-key"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Deck with ID 'invalid-deck-id' not found"));
    }

    @Test
    @DisplayName("Should return 409 when deck is in use by a game")
    void deleteDeck_whenDeckInUse_returns409() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      String deckId = "deck-123";
      doThrow(new DeckInUseException(deckId)).when(deckService).deleteDeck(deckId);

      // When/Then
      mockMvc
          .perform(delete("/decks/{deckId}", deckId).header("X-API-Key", "valid-key"))
          .andExpect(status().isConflict())
          .andExpect(
              jsonPath("$.message")
                  .value("Deck with ID 'deck-123' is in use by a game and cannot be deleted"));
    }

    @Test
    @DisplayName("Should return 204 when deck is deleted successfully")
    void deleteDeck_whenDeckNotInUse_returnsNoContent() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
      doNothing().when(deckService).deleteDeck("deck-123");

      // When/Then
      mockMvc
          .perform(delete("/decks/{deckId}", "deck-123").header("X-API-Key", "valid-key"))
          .andExpect(status().isNoContent());

      verify(deckService, times(1)).deleteDeck("deck-123");
    }
  }

  @Nested
  @DisplayName("POST /games/{gameId}/deck/{deckId} - Add Existing Deck to Game")
  class AddDeckTests {

    @Test
    @DisplayName("Should return 401 when API key is missing")
    void addDeck_withoutApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(
              new com.cardgame.exception.UnauthorizedException(
                  "API Key is required. Provide X-API-Key header."));

      // When/Then
      mockMvc
          .perform(post("/games/{gameId}/deck/{deckId}", "game-123", "deck-456"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.status").value(401))
          .andExpect(jsonPath("$.message").value("API Key is required. Provide X-API-Key header."));

      verify(deckService, never()).addDeckToGame(anyString(), anyString());
    }

    @Test
    @DisplayName("Should return 401 when API key is invalid")
    void addDeck_withInvalidApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(new com.cardgame.exception.UnauthorizedException("Invalid API Key."));

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/deck/{deckId}", "game-123", "deck-456")
                  .header("X-API-Key", "invalid-key"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("Invalid API Key."));

      verify(deckService, never()).addDeckToGame(anyString(), anyString());
    }

    @Test
    @DisplayName("Should return 404 when game does not exist")
    void addDeck_whenGameNotFound_returns404() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      String gameId = "invalid-game-id";
      String deckId = "deck-456";
      doThrow(new GameNotFoundException(gameId)).when(deckService).addDeckToGame(gameId, deckId);

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/deck/{deckId}", gameId, deckId)
                  .header("X-API-Key", "valid-key"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Game with ID 'invalid-game-id' not found"));
    }

    @Test
    @DisplayName("Should return 404 when deck does not exist")
    void addDeck_whenDeckNotFound_returns404() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      String gameId = "game-123";
      String deckId = "invalid-deck-id";
      doThrow(new DeckNotFoundException(deckId)).when(deckService).addDeckToGame(gameId, deckId);

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/deck/{deckId}", gameId, deckId)
                  .header("X-API-Key", "valid-key"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Deck with ID 'invalid-deck-id' not found"));
    }

    @Test
    @DisplayName("Should return 204 when existing deck is added to game successfully")
    void addDeck_withValidGameIdAndDeckId_returnsNoContent() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
      doNothing().when(deckService).addDeckToGame("game-123", "deck-456");

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/deck/{deckId}", "game-123", "deck-456")
                  .header("X-API-Key", "valid-key"))
          .andExpect(status().isNoContent());

      verify(deckService, times(1)).addDeckToGame("game-123", "deck-456");
    }
  }

  @Nested
  @DisplayName("POST /games/{gameId}/deck/shuffle - Shuffle Game Deck")
  class ShuffleDeckTests {

    @Test
    @DisplayName("Should return 401 when API key is missing")
    void shuffleDeck_withoutApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(
              new com.cardgame.exception.UnauthorizedException(
                  "API Key is required. Provide X-API-Key header."));

      // When/Then
      mockMvc
          .perform(post("/games/{gameId}/deck/shuffle", "game-123"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("API Key is required. Provide X-API-Key header."));

      verify(deckService, never()).shuffleGameDeck(any());
    }

    @Test
    @DisplayName("Should return 401 when API key is invalid")
    void shuffleDeck_withInvalidApiKey_returnsUnauthorized() throws Exception {
      // Given - Interceptor throws UnauthorizedException
      when(apiKeyInterceptor.preHandle(any(), any(), any()))
          .thenThrow(new com.cardgame.exception.UnauthorizedException("Invalid API Key."));

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/deck/shuffle", "game-123").header("X-API-Key", "invalid-key"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("Invalid API Key."));

      verify(deckService, never()).shuffleGameDeck(any());
    }

    @Test
    @DisplayName("Should return 204 when deck is shuffled successfully")
    void shuffleDeck_withValidGameId_returnsNoContent() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
      doNothing().when(deckService).shuffleGameDeck("game-123");

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/deck/shuffle", "game-123").header("X-API-Key", "valid-key"))
          .andExpect(status().isNoContent());

      verify(deckService, times(1)).shuffleGameDeck("game-123");
    }

    @Test
    @DisplayName("Should return 204 even when deck is empty (no exception)")
    void shuffleDeck_whenDeckIsEmpty_returnsNoContent() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
      doNothing().when(deckService).shuffleGameDeck("game-123");

      // When/Then
      mockMvc
          .perform(
              post("/games/{gameId}/deck/shuffle", "game-123").header("X-API-Key", "valid-key"))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 when game does not exist")
    void shuffleDeck_whenGameNotFound_returns404() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      String gameId = "invalid-game-id";
      doThrow(new GameNotFoundException(gameId)).when(deckService).shuffleGameDeck(gameId);

      // When/Then
      mockMvc
          .perform(post("/games/{gameId}/deck/shuffle", gameId).header("X-API-Key", "valid-key"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Game with ID 'invalid-game-id' not found"));
    }
  }

  @Nested
  @DisplayName("GET /games/{gameId}/deck/suits-count - Get Suit Counts")
  class GetSuitCountsTests {

    @Test
    @DisplayName("Should return 200 with suit counts when deck has cards")
    void getSuitCounts_withCardsInDeck_returnsCount() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      Map<String, Integer> suitCounts =
          Map.of("HEARTS", 13, "SPADES", 13, "CLUBS", 13, "DIAMONDS", 13);

      when(deckService.getSuitCounts("game-123")).thenReturn(suitCounts);

      // When/Then
      mockMvc
          .perform(get("/games/{gameId}/deck/suits-count", "game-123"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.HEARTS").value(13))
          .andExpect(jsonPath("$.SPADES").value(13))
          .andExpect(jsonPath("$.CLUBS").value(13))
          .andExpect(jsonPath("$.DIAMONDS").value(13));

      verify(deckService, times(1)).getSuitCounts("game-123");
    }

    @Test
    @DisplayName("Should return 200 with empty map when deck is empty")
    void getSuitCounts_whenDeckIsEmpty_returnsEmptyMap() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
      when(deckService.getSuitCounts("game-123")).thenReturn(Map.of());

      // When/Then
      mockMvc
          .perform(get("/games/{gameId}/deck/suits-count", "game-123"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should return 404 when game does not exist")
    void getSuitCounts_whenGameNotFound_returns404() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      String gameId = "invalid-game-id";
      when(deckService.getSuitCounts(gameId)).thenThrow(new GameNotFoundException(gameId));

      // When/Then
      mockMvc
          .perform(get("/games/{gameId}/deck/suits-count", gameId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Game with ID 'invalid-game-id' not found"));
    }
  }

  @Nested
  @DisplayName("GET /games/{gameId}/deck/cards-count - Get Card Counts")
  class GetCardCountsTests {

    @Test
    @DisplayName("Should return 200 with card counts when deck has cards")
    void getCardCounts_withCardsInDeck_returnsCount() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      Map<String, Integer> cardCounts = Map.of("HEARTS-ACE", 1, "HEARTS-TWO", 1, "SPADES-KING", 1);

      when(deckService.getCardCounts("game-123")).thenReturn(cardCounts);

      // When/Then
      mockMvc
          .perform(get("/games/{gameId}/deck/cards-count", "game-123"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$['HEARTS-ACE']").value(1))
          .andExpect(jsonPath("$['HEARTS-TWO']").value(1))
          .andExpect(jsonPath("$['SPADES-KING']").value(1));

      verify(deckService, times(1)).getCardCounts("game-123");
    }

    @Test
    @DisplayName("Should return 200 with counts > 1 when multiple decks are added")
    void getCardCounts_withMultipleDecks_returnsCountsGreaterThanOne() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      // Two decks = each card appears twice
      Map<String, Integer> cardCounts = Map.of("HEARTS-ACE", 2, "HEARTS-TWO", 2, "SPADES-KING", 2);

      when(deckService.getCardCounts("game-123")).thenReturn(cardCounts);

      // When/Then
      mockMvc
          .perform(get("/games/{gameId}/deck/cards-count", "game-123"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$['HEARTS-ACE']").value(2))
          .andExpect(jsonPath("$['HEARTS-TWO']").value(2))
          .andExpect(jsonPath("$['SPADES-KING']").value(2));
    }

    @Test
    @DisplayName("Should return 200 with empty map when deck is empty")
    void getCardCounts_whenDeckIsEmpty_returnsEmptyMap() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
      when(deckService.getCardCounts("game-123")).thenReturn(Map.of());

      // When/Then
      mockMvc
          .perform(get("/games/{gameId}/deck/cards-count", "game-123"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());
    }
  }
}
