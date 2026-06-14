package com.cardgame.controller;

import com.cardgame.annotation.AuthApiKey;
import com.cardgame.model.dto.AddPlayerRequest;
import com.cardgame.model.dto.PlayerScoreResponse;
import com.cardgame.model.entity.Card;
import com.cardgame.model.entity.Player;
import com.cardgame.service.PlayerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games/{gameId}/players")
@Validated
@RequiredArgsConstructor
public class PlayerController {

  private final PlayerService playerService;

  @PostMapping
  @AuthApiKey
  public ResponseEntity<Void> addPlayerToGame(
      @PathVariable String gameId, @Valid @RequestBody AddPlayerRequest request) {
    Player player = Player.createNew(request.getName());
    playerService.addPlayer(gameId, player);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{playerId}")
  @AuthApiKey
  public ResponseEntity<Void> removePlayerFromGame(
      @PathVariable String gameId, @PathVariable String playerId) {
    playerService.removePlayer(gameId, playerId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{playerId}/deal")
  @AuthApiKey
  public ResponseEntity<List<Card>> dealCardsToPlayer(
      @PathVariable String gameId,
      @PathVariable String playerId,
      @RequestParam(defaultValue = "1") @Positive(message = "Count must be positive") int count) {
    List<Card> dealtCards = playerService.dealCards(gameId, playerId, count);
    return ResponseEntity.ok(dealtCards);
  }

  @GetMapping("/{playerId}/cards")
  public ResponseEntity<List<Card>> getPlayerCards(
      @PathVariable String gameId, @PathVariable String playerId) {
    return ResponseEntity.ok(playerService.getPlayerCards(gameId, playerId));
  }

  @GetMapping("/scores")
  public ResponseEntity<List<PlayerScoreResponse>> getPlayerScores(@PathVariable String gameId) {
    return ResponseEntity.ok(playerService.getPlayerScores(gameId));
  }
}
