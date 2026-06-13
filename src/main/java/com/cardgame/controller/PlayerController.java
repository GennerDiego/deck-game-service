package com.cardgame.controller;

import com.cardgame.model.dto.AddPlayerRequest;
import com.cardgame.model.entity.Player;
import com.cardgame.service.GameService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games/{gameId}/players")
@Validated
public class PlayerController {

  @Autowired private GameService gameService;

  @PostMapping
  public ResponseEntity<Void> addPlayerToGame(
      @PathVariable String gameId, @Valid @RequestBody AddPlayerRequest request) {
    Player player = Player.createNew(request.getName());
    gameService.addPlayer(gameId, player);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{playerId}")
  public ResponseEntity<Void> removePlayerFromGame(
      @PathVariable String gameId, @PathVariable String playerId) {
    gameService.removePlayer(gameId, playerId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{playerId}/deal")
  public ResponseEntity<java.util.List<com.cardgame.model.entity.Card>> dealCardsToPlayer(
      @PathVariable String gameId,
      @PathVariable String playerId,
      @RequestParam(defaultValue = "1") @Positive(message = "Count must be positive") int count) {
    java.util.List<com.cardgame.model.entity.Card> dealtCards =
        gameService.dealCards(gameId, playerId, count);
    return ResponseEntity.ok(dealtCards);
  }

  @GetMapping("/{playerId}/cards")
  public ResponseEntity<java.util.List<com.cardgame.model.entity.Card>> getPlayerCards(
      @PathVariable String gameId, @PathVariable String playerId) {
    return ResponseEntity.ok(gameService.getPlayerCards(gameId, playerId));
  }

  @GetMapping("/scores")
  public ResponseEntity<java.util.List<com.cardgame.model.dto.PlayerScoreResponse>> getPlayerScores(
      @PathVariable String gameId) {
    return ResponseEntity.ok(gameService.getPlayerScores(gameId));
  }
}
