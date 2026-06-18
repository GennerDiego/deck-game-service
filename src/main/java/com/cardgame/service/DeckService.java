package com.cardgame.service;

import com.cardgame.exception.DeckInUseException;
import com.cardgame.exception.DeckNotFoundException;
import com.cardgame.model.entity.Deck;
import com.cardgame.model.entity.Game;
import com.cardgame.repository.DeckRepository;
import com.cardgame.repository.GameRepository;
import com.cardgame.util.ShuffleUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeckService {

  private final DeckRepository deckRepository;
  private final GameRepository gameRepository;
  private final GameService gameService;
  private final ShuffleUtil shuffleUtil;
  private final DistributedLockService lockService;

  /**
   * Create a new deck (standalone, not attached to any game)
   *
   * @return created Deck
   */
  public Deck createDeck() {
    Deck deck = Deck.createNew();
    deckRepository.save(deck);
    log.info("Deck created with ID: {}", deck.getId());
    return deck;
  }

  /**
   * Find deck by ID
   *
   * @param deckId deck ID
   * @return Deck
   * @throws DeckNotFoundException if deck not found
   */
  public Deck findById(String deckId) {
    return deckRepository.findById(deckId).orElseThrow(() -> new DeckNotFoundException(deckId));
  }

  /**
   * Get all decks
   *
   * @return list of all decks
   */
  public List<Deck> getAllDecks() {
    return deckRepository.findAll();
  }

  /**
   * Add an existing deck to a game
   *
   * @param gameId game ID
   * @param deckId deck ID
   * @throws DeckNotFoundException if deck not found
   * @throws com.cardgame.exception.GameNotFoundException if game not found
   */
  public void addDeckToGame(String gameId, String deckId) {
    String lockKey = "game:" + gameId;

    lockService.executeWithLock(
        lockKey,
        () -> {
          Game game = gameService.findById(gameId);
          Deck deck = findById(deckId);

          game.addDeck(deck);
          gameRepository.save(game);
          log.info(
              "Deck {} added to game {}. Total cards: {}",
              deckId,
              gameId,
              game.getGameDeck().size());
        });
  }

  /**
   * Delete a deck (only if not in use by any game)
   *
   * @param deckId deck ID
   * @throws DeckNotFoundException if deck not found
   * @throws DeckInUseException if deck is in use by a game
   */
  public void deleteDeck(String deckId) {
    Deck deck = findById(deckId);

    // Check if deck is in use by any game
    List<Game> allGames = gameRepository.findAll();
    boolean isInUse = allGames.stream().anyMatch(game -> game.getDeckIdsInUse().contains(deckId));

    if (isInUse) {
      throw new DeckInUseException(deckId);
    }

    deckRepository.deleteById(deckId);
    log.info("Deck {} deleted", deckId);
  }

  public void shuffleGameDeck(String gameId) {
    String lockKey = "game:" + gameId;

    lockService.executeWithLock(
        lockKey,
        () -> {
          Game game = gameService.findById(gameId);

          // Shuffle on empty deck does nothing (doesn't throw exception)
          if (!game.getGameDeck().isEmpty()) {
            shuffleUtil.shuffle(game.getGameDeck());
            gameRepository.save(game);
            log.info("Game deck shuffled for game: {}", gameId);
          } else {
            log.debug("Shuffle called on empty deck for game: {}", gameId);
          }
        });
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
