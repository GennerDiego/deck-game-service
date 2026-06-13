package com.cardgame.service;

import com.cardgame.exception.*;
import com.cardgame.model.entity.Card;
import com.cardgame.model.entity.Deck;
import com.cardgame.model.entity.Game;
import com.cardgame.model.entity.Player;
import com.cardgame.repository.GameRepository;
import com.cardgame.util.ShuffleUtil;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameService {

  private static final Logger log = LoggerFactory.getLogger(GameService.class);

  @Autowired private GameRepository gameRepository;

  @Autowired private ShuffleUtil shuffleUtil;

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

  public void addDeckToGame(String gameId) {
    Game game = findById(gameId);

    Deck deck = Deck.createNew();
    game.addDeck(deck);
    gameRepository.save(game);
    log.info("Deck added to game {}. Total cards: {}", gameId, game.getGameDeck().size());
  }

  public void shuffleGameDeck(String gameId) {
    Game game = findById(gameId);

    // Shuffle em deck vazio não faz nada (não lança exceção)
    if (!game.getGameDeck().isEmpty()) {
      shuffleUtil.shuffle(game.getGameDeck());
      gameRepository.save(game);
      log.info("Game deck shuffled for game: {}", gameId);
    } else {
      log.debug("Shuffle called on empty deck for game: {}", gameId);
    }
  }

  public void addPlayer(String gameId, Player player) {
    Game game = findById(gameId);

    if (game.hasPlayer(player.getId())) {
      throw new DuplicatePlayerException(player.getId(), gameId);
    }

    game.addPlayer(player);
    gameRepository.save(game);
    log.info("Player {} added to game {}", player.getId(), gameId);
  }

  public void removePlayer(String gameId, String playerId) {
    Game game = findById(gameId);

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

    Game game = findById(gameId);

    if (!game.hasPlayer(playerId)) {
      throw new PlayerNotFoundException(playerId, gameId);
    }

    // Se deck está vazio, retorna lista vazia (não lança exceção)
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
    Game game = findById(gameId);

    if (!game.hasPlayer(playerId)) {
      throw new PlayerNotFoundException(playerId, gameId);
    }

    return game.getPlayer(playerId).getCards();
  }

  public List<com.cardgame.model.dto.PlayerScoreResponse> getPlayerScores(String gameId) {
    Game game = findById(gameId);

    return game.getPlayers().values().stream()
        .map(
            player ->
                com.cardgame.model.dto.PlayerScoreResponse.of(
                    player.getId(),
                    player.getName(),
                    player.getTotalValue(),
                    player.getCardCount()))
        .sorted((a, b) -> Integer.compare(b.getTotalValue(), a.getTotalValue()))
        .collect(Collectors.toList());
  }

  public Map<String, Integer> getSuitCounts(String gameId) {
    Game game = findById(gameId);

    Map<String, Integer> suitCounts = new HashMap<>();
    suitCounts.put("HEARTS", 0);
    suitCounts.put("SPADES", 0);
    suitCounts.put("CLUBS", 0);
    suitCounts.put("DIAMONDS", 0);

    for (Card card : game.getGameDeck()) {
      String suit = card.getSuit().name();
      suitCounts.put(suit, suitCounts.get(suit) + 1);
    }

    return suitCounts;
  }

  public Map<String, Integer> getCardCounts(String gameId) {
    Game game = findById(gameId);

    Map<String, Integer> cardCounts = new LinkedHashMap<>();

    for (Card card : game.getGameDeck()) {
      String cardKey = card.getRank() + " of " + card.getSuit();
      cardCounts.put(cardKey, cardCounts.getOrDefault(cardKey, 0) + 1);
    }

    return cardCounts.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }

  public Game getGameDetails(String gameId) {
    return findById(gameId);
  }
}
