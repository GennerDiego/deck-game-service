package com.cardgame.service;

import com.cardgame.exception.*;
import com.cardgame.model.dto.PlayerScoreResponse;
import com.cardgame.model.entity.Card;
import com.cardgame.model.entity.Game;
import com.cardgame.model.entity.Player;
import com.cardgame.repository.GameRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

  private final GameRepository gameRepository;
  private final GameService gameService;

  public void addPlayer(String gameId, Player player) {
    Game game = gameService.findById(gameId);

    if (game.hasPlayer(player.getId())) {
      throw new DuplicatePlayerException(player.getId(), gameId);
    }

    game.addPlayer(player);
    gameRepository.save(game);
    log.info("Player {} added to game {}", player.getId(), gameId);
  }

  public void removePlayer(String gameId, String playerId) {
    Game game = gameService.findById(gameId);

    if (!game.hasPlayer(playerId)) {
      throw new PlayerNotFoundException(playerId, gameId);
    }

    game.removePlayer(playerId);
    gameRepository.save(game);
    log.info("Player {} removed from game {}", playerId, gameId);
  }

  public List<Card> dealCards(String gameId, String playerId, int count) {
    if (count <= 0) {
      throw InvalidDealOperationException.invalidCount(count);
    }

    Game game = gameService.findById(gameId);

    if (!game.hasPlayer(playerId)) {
      throw new PlayerNotFoundException(playerId, gameId);
    }

    // If deck is empty, return empty list (doesn't throw exception)
    int cardsToDeal = Math.min(count, game.getGameDeck().size());
    List<Card> dealtCards = new ArrayList<>();
    Player player = game.getPlayer(playerId);

    for (int i = 0; i < cardsToDeal; i++) {
      Card card = game.getGameDeck().remove(0);
      dealtCards.add(card);
      player.getCards().add(card);
    }

    gameRepository.save(game);
    log.debug(
        "Dealt {} cards to player {} in game {}. Remaining cards: {}",
        cardsToDeal,
        playerId,
        gameId,
        game.getGameDeck().size());

    return dealtCards;
  }

  public List<Card> getPlayerCards(String gameId, String playerId) {
    Game game = gameService.findById(gameId);

    if (!game.hasPlayer(playerId)) {
      throw new PlayerNotFoundException(playerId, gameId);
    }

    return game.getPlayer(playerId).getCards();
  }

  public List<PlayerScoreResponse> getPlayerScores(String gameId) {
    Game game = gameService.findById(gameId);

    return game.getPlayers().stream()
        .map(
            player ->
                PlayerScoreResponse.of(
                    player.getId(),
                    player.getName(),
                    player.getTotalValue(),
                    player.getCardCount()))
        .sorted((a, b) -> Integer.compare(b.getTotalValue(), a.getTotalValue()))
        .collect(Collectors.toList());
  }
}
