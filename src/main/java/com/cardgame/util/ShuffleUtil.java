package com.cardgame.util;

import com.cardgame.model.entity.Card;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShuffleUtil {

  private static final Logger log = LoggerFactory.getLogger(ShuffleUtil.class);
  private final Random random;

  public ShuffleUtil() {
    this.random = new Random();
  }

  /**
   * Shuffle a list of cards using the Fisher-Yates algorithm.
   *
   * <p>Algorithm complexity: O(n) Time: O(n) where n is the number of cards Space: O(1) - in-place
   * shuffling
   *
   * <p>The Fisher-Yates shuffle ensures each permutation is equally likely by iterating backwards
   * through the list and swapping each element with a randomly chosen element from the remaining
   * unshuffled portion.
   *
   * @param cards List of cards to shuffle (modified in-place)
   */
  public void shuffle(List<Card> cards) {
    if (cards == null || cards.isEmpty()) {
      log.trace("Attempted to shuffle null or empty card list");
      return;
    }

    int totalCards = cards.size();
    log.debug("Starting Fisher-Yates shuffle for {} cards", totalCards);

    // Iterate backwards from last element to second element
    for (int currentIndex = totalCards - 1; currentIndex > 0; currentIndex--) {
      // Choose random index from 0 to currentIndex (inclusive)
      int randomIndex = random.nextInt(currentIndex + 1);

      // Swap cards at currentIndex and randomIndex
      Card temporaryCard = cards.get(currentIndex);
      cards.set(currentIndex, cards.get(randomIndex));
      cards.set(randomIndex, temporaryCard);

      log.trace("Swapped card at index {} with index {}", currentIndex, randomIndex);
    }

    log.debug("Shuffle completed for {} cards", totalCards);
  }
}
