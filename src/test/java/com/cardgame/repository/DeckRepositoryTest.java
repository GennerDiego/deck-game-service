package com.cardgame.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cardgame.model.entity.Deck;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeckRepository - Unit Tests")
class DeckRepositoryTest {

  @Mock private RedisTemplate<String, String> redisTemplate;

  @Mock private ValueOperations<String, String> valueOperations;

  private ObjectMapper objectMapper;
  private DeckRepository deckRepository;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.configure(
        com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    deckRepository = new DeckRepository(redisTemplate, objectMapper);
  }

  @Nested
  @DisplayName("save() - Persist deck")
  class SaveTests {

    @Test
    @DisplayName("Should save deck to Redis with correct key prefix")
    void save_withValidDeck_savesToRedis() throws Exception {
      // Given
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      Deck deck = Deck.createNew();
      String expectedKey = "deck:" + deck.getId();

      // When
      deckRepository.save(deck);

      // Then
      verify(valueOperations).set(eq(expectedKey), anyString(), any(Duration.class));
    }
  }

  @Nested
  @DisplayName("findById() - Retrieve deck")
  class FindByIdTests {

    @Test
    @DisplayName("Should return deck when found in Redis")
    void findById_whenDeckExists_returnsDeck() throws Exception {
      // Given
      Deck deck = Deck.createNew();
      String deckId = deck.getId();
      String key = "deck:" + deckId;
      String json = objectMapper.writeValueAsString(deck);

      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      when(valueOperations.get(key)).thenReturn(json);

      // When
      Optional<Deck> result = deckRepository.findById(deckId);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(deckId);
    }

    @Test
    @DisplayName("Should return empty when deck not found")
    void findById_whenDeckNotFound_returnsEmpty() {
      // Given
      String deckId = "non-existent-deck";
      String key = "deck:" + deckId;

      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      when(valueOperations.get(key)).thenReturn(null);

      // When
      Optional<Deck> result = deckRepository.findById(deckId);

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("deleteById() - Remove deck")
  class DeleteByIdTests {

    @Test
    @DisplayName("Should delete deck from Redis")
    void deleteById_withValidId_deletesFromRedis() {
      // Given
      String deckId = "deck-123";
      String key = "deck:" + deckId;

      when(redisTemplate.delete(key)).thenReturn(true);

      // When
      deckRepository.deleteById(deckId);

      // Then
      verify(redisTemplate, times(1)).delete(key);
    }
  }

  @Nested
  @DisplayName("findAll() - Retrieve all decks")
  class FindAllTests {

    @Test
    @DisplayName("Should return empty list when no decks exist")
    void findAll_whenNoDecks_returnsEmptyList() {
      // Given
      when(redisTemplate.keys("deck:*")).thenReturn(Set.of());

      // When
      List<Deck> result = deckRepository.findAll();

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return all decks")
    void findAll_whenDecksExist_returnsAllDecks() throws Exception {
      // Given
      Deck deck1 = Deck.createNew();
      Deck deck2 = Deck.createNew();

      String key1 = "deck:" + deck1.getId();
      String key2 = "deck:" + deck2.getId();

      String json1 = objectMapper.writeValueAsString(deck1);
      String json2 = objectMapper.writeValueAsString(deck2);

      when(redisTemplate.keys("deck:*")).thenReturn(Set.of(key1, key2));
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      when(valueOperations.get(key1)).thenReturn(json1);
      when(valueOperations.get(key2)).thenReturn(json2);

      // When
      List<Deck> result = deckRepository.findAll();

      // Then
      assertThat(result).hasSize(2);
      assertThat(result)
          .extracting(Deck::getId)
          .containsExactlyInAnyOrder(deck1.getId(), deck2.getId());
    }
  }

  @Test
  @DisplayName("Should use 'deck:' as key prefix")
  void getKeyPrefix_shouldReturnDeckPrefix() {
    // When - Create instance (key prefix is set in constructor)
    ObjectMapper mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    DeckRepository repo = new DeckRepository(redisTemplate, mapper);

    // Then - Verify by attempting to find - it will use the correct prefix
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("deck:test-id")).thenReturn(null);

    repo.findById("test-id");

    verify(valueOperations).get("deck:test-id");
  }
}
