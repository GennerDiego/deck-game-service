package com.cardgame.util;

import static org.assertj.core.api.Assertions.*;

import com.cardgame.model.entity.Card;
import com.cardgame.model.entity.Rank;
import com.cardgame.model.entity.Suit;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ShuffleUtil - Unit Tests")
class ShuffleUtilTest {

  private ShuffleUtil shuffleUtil;

  @BeforeEach
  void setUp() {
    shuffleUtil = new ShuffleUtil();
  }

  @Nested
  @DisplayName("shuffle() - Fisher-Yates Algorithm")
  class ShuffleTests {

    @Test
    @DisplayName("Should handle null list without error")
    void shuffle_withNullList_doesNotThrowException() {
      // When/Then
      assertThatCode(() -> shuffleUtil.shuffle(null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle empty list without error")
    void shuffle_withEmptyList_doesNotThrowException() {
      // Given
      List<Card> emptyCards = new ArrayList<>();

      // When/Then
      assertThatCode(() -> shuffleUtil.shuffle(emptyCards)).doesNotThrowAnyException();
      assertThat(emptyCards).isEmpty();
    }

    @Test
    @DisplayName("Should not modify single card list")
    void shuffle_withSingleCard_doesNotModify() {
      // Given
      Card card = new Card(Suit.HEARTS, Rank.ACE);
      List<Card> cards = new ArrayList<>(List.of(card));

      // When
      shuffleUtil.shuffle(cards);

      // Then
      assertThat(cards).hasSize(1);
      assertThat(cards.get(0)).isEqualTo(card);
    }

    @Test
    @DisplayName("Should shuffle cards and change order")
    void shuffle_withMultipleCards_changesOrder() {
      // Given - Create ordered deck
      List<Card> cards = new ArrayList<>();
      for (Suit suit : Suit.values()) {
        for (Rank rank : Rank.values()) {
          cards.add(new Card(suit, rank));
        }
      }
      List<Card> originalOrder = new ArrayList<>(cards);

      // When
      shuffleUtil.shuffle(cards);

      // Then - Should still have all cards but in different order
      assertThat(cards).hasSize(52);
      assertThat(cards).containsExactlyInAnyOrderElementsOf(originalOrder);

      // With 52 cards, probability of same order after shuffle is ~1/52! (extremely unlikely)
      // Check that at least some cards changed position
      int differentPositions = 0;
      for (int i = 0; i < cards.size(); i++) {
        if (!cards.get(i).equals(originalOrder.get(i))) {
          differentPositions++;
        }
      }
      assertThat(differentPositions)
          .as("At least 40 out of 52 cards should be in different positions")
          .isGreaterThan(40);
    }

    @Test
    @DisplayName("Should preserve all cards after shuffle")
    void shuffle_withStandardDeck_preservesAllCards() {
      // Given
      List<Card> cards = new ArrayList<>();
      cards.add(new Card(Suit.HEARTS, Rank.ACE));
      cards.add(new Card(Suit.SPADES, Rank.KING));
      cards.add(new Card(Suit.CLUBS, Rank.QUEEN));
      cards.add(new Card(Suit.DIAMONDS, Rank.JACK));
      List<Card> originalCards = new ArrayList<>(cards);

      // When
      shuffleUtil.shuffle(cards);

      // Then - Same cards, possibly different order
      assertThat(cards).hasSize(4);
      assertThat(cards).containsExactlyInAnyOrderElementsOf(originalCards);
    }

    @Test
    @DisplayName("Should shuffle two-card deck")
    void shuffle_withTwoCards_swapsOrKeepsOrder() {
      // Given
      Card card1 = new Card(Suit.HEARTS, Rank.ACE);
      Card card2 = new Card(Suit.SPADES, Rank.KING);
      List<Card> cards = new ArrayList<>(List.of(card1, card2));

      // When - Shuffle multiple times to test randomness
      int sameOrderCount = 0;
      int swappedOrderCount = 0;

      for (int i = 0; i < 100; i++) {
        List<Card> testCards = new ArrayList<>(List.of(card1, card2));
        shuffleUtil.shuffle(testCards);

        if (testCards.get(0).equals(card1)) {
          sameOrderCount++;
        } else {
          swappedOrderCount++;
        }
      }

      // Then - Should have mixed results (not all same order, not all swapped)
      assertThat(sameOrderCount).isGreaterThan(0);
      assertThat(swappedOrderCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should produce different results on multiple shuffles")
    void shuffle_calledMultipleTimes_producesDifferentResults() {
      // Given - Same starting deck
      List<Card> cards1 = createStandardDeck();
      List<Card> cards2 = createStandardDeck();
      List<Card> cards3 = createStandardDeck();

      // When
      shuffleUtil.shuffle(cards1);
      shuffleUtil.shuffle(cards2);
      shuffleUtil.shuffle(cards3);

      // Then - At least one shuffle should produce different order
      boolean allSame = cards1.equals(cards2) && cards2.equals(cards3) && cards1.equals(cards3);
      assertThat(allSame).as("Multiple shuffles should produce different results").isFalse();
    }

    @Test
    @DisplayName("Should shuffle in-place without creating new list")
    void shuffle_modifiesListInPlace() {
      // Given
      List<Card> cards = new ArrayList<>();
      cards.add(new Card(Suit.HEARTS, Rank.ACE));
      cards.add(new Card(Suit.SPADES, Rank.KING));
      cards.add(new Card(Suit.CLUBS, Rank.QUEEN));

      List<Card> sameReference = cards;

      // When
      shuffleUtil.shuffle(cards);

      // Then
      assertThat(cards).isSameAs(sameReference);
    }
  }

  // Helper method
  private List<Card> createStandardDeck() {
    List<Card> cards = new ArrayList<>();
    for (Suit suit : Suit.values()) {
      for (Rank rank : Rank.values()) {
        cards.add(new Card(suit, rank));
      }
    }
    return cards;
  }
}
