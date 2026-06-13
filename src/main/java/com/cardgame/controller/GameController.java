package com.cardgame.controller;

import com.cardgame.model.entity.Game;
import com.cardgame.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
public class GameController {

  @Autowired private GameService gameService;

  @PostMapping
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
  public ResponseEntity<Void> deleteGame(@PathVariable String gameId) {
    gameService.deleteGame(gameId);
    return ResponseEntity.noContent().build();
  }
}
