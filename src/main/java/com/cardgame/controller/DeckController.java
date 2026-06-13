package com.cardgame.controller;

import com.cardgame.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games/{gameId}/decks")
public class DeckController {

  @Autowired private GameService gameService;

  @PostMapping
  public ResponseEntity<Void> addDeckToGame(@PathVariable String gameId) {
    gameService.addDeckToGame(gameId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/shuffle")
  public ResponseEntity<Void> shuffleGameDeck(@PathVariable String gameId) {
    gameService.shuffleGameDeck(gameId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/suits-count")
  public ResponseEntity<java.util.Map<String, Integer>> getSuitCounts(@PathVariable String gameId) {
    return ResponseEntity.ok(gameService.getSuitCounts(gameId));
  }

  @GetMapping("/cards-count")
  public ResponseEntity<java.util.Map<String, Integer>> getCardCounts(@PathVariable String gameId) {
    return ResponseEntity.ok(gameService.getCardCounts(gameId));
  }
}
