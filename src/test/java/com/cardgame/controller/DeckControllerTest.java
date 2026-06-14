package com.cardgame.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.cardgame.exception.GameNotFoundException;
import com.cardgame.interceptor.ApiKeyInterceptor;
import com.cardgame.service.DeckService;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DeckController.class)
@DisplayName("DeckController - Unit Tests")
class DeckControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private DeckService deckService;

  @MockBean private ApiKeyInterceptor apiKeyInterceptor;

  @Nested
  @DisplayName("POST /games/{gameId}/decks - Add Deck to Game")
  class AddDeckTests {

    @Test
    @DisplayName("Should return 404 when game does not exist")
    void addDeck_whenGameNotFound_returns404() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);

      String gameId = "invalid-game-id";
      doThrow(new GameNotFoundException(gameId)).when(deckService).addDeckToGame(gameId);

      // When/Then
      mockMvc
          .perform(post("/games/{gameId}/decks", gameId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Game with ID 'invalid-game-id' not found"));
    }

    @Test
    @DisplayName("Should return 204 when deck is added successfully")
    void addDeck_withValidGameId_returnsNoContent() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
      doNothing().when(deckService).addDeckToGame("game-123");

      // When/Then
      mockMvc.perform(post("/games/{gameId}/decks", "game-123")).andExpect(status().isNoContent());

      verify(deckService, times(1)).addDeckToGame("game-123");
    }
  }

  @Nested
  @DisplayName("POST /games/{gameId}/decks/shuffle - Shuffle Game Deck")
  class ShuffleDeckTests {

    @Test
    @DisplayName("Should return 204 when deck is shuffled successfully")
    void shuffleDeck_withValidGameId_returnsNoContent() throws Exception {
      // Given
      when(apiKeyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
      doNothing().when(deckService).shuffleGameDeck("game-123");

      // When/Then
      mockMvc
          .perform(post("/games/{gameId}/decks/shuffle", "game-123"))
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
          .perform(post("/games/{gameId}/decks/shuffle", "game-123"))
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
          .perform(post("/games/{gameId}/decks/shuffle", gameId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Game with ID 'invalid-game-id' not found"));
    }
  }

  @Nested
  @DisplayName("GET /games/{gameId}/decks/suits-count - Get Suit Counts")
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
          .perform(get("/games/{gameId}/decks/suits-count", "game-123"))
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
          .perform(get("/games/{gameId}/decks/suits-count", "game-123"))
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
          .perform(get("/games/{gameId}/decks/suits-count", gameId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Game with ID 'invalid-game-id' not found"));
    }
  }

  @Nested
  @DisplayName("GET /games/{gameId}/decks/cards-count - Get Card Counts")
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
          .perform(get("/games/{gameId}/decks/cards-count", "game-123"))
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
          .perform(get("/games/{gameId}/decks/cards-count", "game-123"))
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
          .perform(get("/games/{gameId}/decks/cards-count", "game-123"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());
    }
  }
}
