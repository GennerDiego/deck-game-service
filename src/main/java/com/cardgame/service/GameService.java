package com.cardgame.service;

import com.cardgame.exception.GameNotFoundException;
import com.cardgame.model.entity.Game;
import com.cardgame.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

  private final GameRepository gameRepository;

  public Game createGame() {
    Game game = Game.createNew();
    game = gameRepository.save(game);
    log.info("Game created with ID: {}", game.getId());
    return game;
  }

  public Game findById(String gameId) {
    return gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
  }

  public void deleteGame(String gameId) {
    if (!gameRepository.existsById(gameId)) {
      throw new GameNotFoundException(gameId);
    }
    gameRepository.deleteById(gameId);
    log.info("Game deleted: {}", gameId);
  }
}
