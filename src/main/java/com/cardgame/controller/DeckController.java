package com.cardgame.controller;

import com.cardgame.annotation.AuthApiKey;
import com.cardgame.model.entity.Deck;
import com.cardgame.service.DeckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "Deck Management", description = "Manage deck templates and game shoe operations")
public class DeckController {

  private final DeckService deckService;

  @PostMapping("/decks")
  @AuthApiKey
  @Operation(
      summary = "Create a new deck template",
      description = "Creates a standard 52-card deck that can be reused across multiple games")
  public ResponseEntity<Deck> createDeck() {
    Deck deck = deckService.createDeck();
    return ResponseEntity.status(HttpStatus.CREATED).body(deck);
  }

  @GetMapping(value = "/decks", produces = "application/json")
  public ResponseEntity<List<Deck>> getAllDecks() {
    return ResponseEntity.ok(deckService.getAllDecks());
  }

  @GetMapping(value = "/decks/{deckId}", produces = "application/json")
  public ResponseEntity<Deck> getDeck(@PathVariable String deckId) {
    return ResponseEntity.ok(deckService.findById(deckId));
  }

  @DeleteMapping("/decks/{deckId}")
  @AuthApiKey
  public ResponseEntity<Void> deleteDeck(@PathVariable String deckId) {
    deckService.deleteDeck(deckId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/games/{gameId}/deck/{deckId}")
  @AuthApiKey
  @Operation(
      summary = "Add deck to game shoe",
      description =
          "Adds a deck's cards to the game's shoe (card collection). The deck template remains"
              + " unchanged while cards are copied into the game.")
  public ResponseEntity<Void> addDeckToGame(
      @PathVariable String gameId, @PathVariable String deckId) {
    deckService.addDeckToGame(gameId, deckId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/games/{gameId}/deck/shuffle")
  @AuthApiKey
  @Operation(
      summary = "Shuffle game deck",
      description =
          "Shuffles the remaining cards in the game's shoe using Fisher-Yates algorithm. Already"
              + " dealt cards are not affected.")
  public ResponseEntity<Void> shuffleGameDeck(@PathVariable String gameId) {
    deckService.shuffleGameDeck(gameId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/games/{gameId}/deck/suits-count")
  @Operation(
      summary = "Count cards by suit",
      description = "Returns the count of undealt cards grouped by suit in the game's shoe")
  public ResponseEntity<Map<String, Integer>> getSuitCounts(@PathVariable String gameId) {
    return ResponseEntity.ok(deckService.getSuitCounts(gameId));
  }

  @GetMapping("/games/{gameId}/deck/cards-count")
  @Operation(
      summary = "Count each card type",
      description =
          "Returns the count of each specific undealt card (e.g., ACE of HEARTS) in the game's shoe")
  public ResponseEntity<Map<String, Integer>> getCardCounts(@PathVariable String gameId) {
    return ResponseEntity.ok(deckService.getCardCounts(gameId));
  }
}
