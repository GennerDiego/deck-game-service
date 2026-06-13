package com.cardgame.service;

import com.cardgame.model.entity.Deck;
import com.cardgame.model.entity.Game;
import com.cardgame.repository.GameRepository;
import com.cardgame.util.ShuffleUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeckService {

  private final GameRepository gameRepository;
  private final GameService gameService;
  private final ShuffleUtil shuffleUtil;

  public void addDeckToGame(String gameId) {
    Game game = gameService.findById(gameId);

    Deck deck = Deck.createNew();
    game.addDeck(deck);
    gameRepository.save(game);
    log.info("Deck added to game {}. Total cards: {}", gameId, game.getGameDeck().size());
  }

  public void shuffleGameDeck(String gameId) {
    Game game = gameService.findById(gameId);

    // Shuffle on empty deck does nothing (doesn't throw exception)
    if (!game.getGameDeck().isEmpty()) {
      shuffleUtil.shuffle(game.getGameDeck());
      gameRepository.save(game);
      log.info("Game deck shuffled for game: {}", gameId);
    } else {
      log.debug("Shuffle called on empty deck for game: {}", gameId);
    }
  }

  public Map<String, Integer> getSuitCounts(String gameId) {
    Game game = gameService.findById(gameId);

    Map<String, Integer> counts = new LinkedHashMap<>();
    counts.put("HEARTS", 0);
    counts.put("SPADES", 0);
    counts.put("CLUBS", 0);
    counts.put("DIAMONDS", 0);

    game.getGameDeck().forEach(card -> counts.merge(card.getSuit().name(), 1, Integer::sum));

    return counts;
  }

  public Map<String, Integer> getCardCounts(String gameId) {
    Game game = gameService.findById(gameId);

    return game.getGameDeck().stream()
        .collect(Collectors.groupingBy(card -> card.toString(), Collectors.summingInt(e -> 1)))
        .entrySet()
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }
}
