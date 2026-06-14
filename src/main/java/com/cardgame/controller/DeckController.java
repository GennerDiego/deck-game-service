package com.cardgame.controller;

import com.cardgame.annotation.AuthApiKey;
import com.cardgame.service.DeckService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games/{gameId}/decks")
@RequiredArgsConstructor
public class DeckController {

  private final DeckService deckService;

  @PostMapping
  @AuthApiKey
  public ResponseEntity<Void> addDeckToGame(@PathVariable String gameId) {
    deckService.addDeckToGame(gameId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/shuffle")
  @AuthApiKey
  public ResponseEntity<Void> shuffleGameDeck(@PathVariable String gameId) {
    deckService.shuffleGameDeck(gameId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/suits-count")
  public ResponseEntity<Map<String, Integer>> getSuitCounts(@PathVariable String gameId) {
    return ResponseEntity.ok(deckService.getSuitCounts(gameId));
  }

  @GetMapping("/cards-count")
  public ResponseEntity<Map<String, Integer>> getCardCounts(@PathVariable String gameId) {
    return ResponseEntity.ok(deckService.getCardCounts(gameId));
  }
}
