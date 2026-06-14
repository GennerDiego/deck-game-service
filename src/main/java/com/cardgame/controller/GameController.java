package com.cardgame.controller;

import com.cardgame.annotation.AuthApiKey;
import com.cardgame.model.entity.Game;
import com.cardgame.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

  private final GameService gameService;

  @PostMapping
  @AuthApiKey
  public ResponseEntity<Game> createGame() {
    Game game = gameService.createGame();
    return ResponseEntity.status(HttpStatus.CREATED).body(game);
  }

  @GetMapping("/{gameId}")
  public ResponseEntity<Game> getGame(@PathVariable String gameId) {
    Game game = gameService.findById(gameId);
    return ResponseEntity.ok(game);
  }

  @DeleteMapping("/{gameId}")
  @AuthApiKey
  public ResponseEntity<Void> deleteGame(@PathVariable String gameId) {
    gameService.deleteGame(gameId);
    return ResponseEntity.noContent().build();
  }
}
