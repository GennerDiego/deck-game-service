package com.cardgame.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cardgame.model.entity.Game;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
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
@DisplayName("JsonRedisRepository - Unit Tests")
class JsonRedisRepositoryTest {

  @Mock private RedisTemplate<String, String> redisTemplate;

  @Mock private ValueOperations<String, String> valueOperations;

  private ObjectMapper objectMapper;
  private TestJsonRedisRepository repository;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules(); // Register JSR310 module for Instant serialization
    objectMapper.configure(
        com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        false); // Ignore unknown properties during deserialization
    repository = new TestJsonRedisRepository(redisTemplate, objectMapper);
  }

  @Nested
  @DisplayName("save() - Persist entity")
  class SaveTests {

    @Test
    @DisplayName("Should save entity as JSON with TTL")
    void save_withValidEntity_savesToRedis() throws Exception {
      // Given
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      Game game = Game.createNew();
      String expectedKey = "game:" + game.getId();
      String expectedJson = objectMapper.writeValueAsString(game);

      // When
      Game result = repository.save(game);

      // Then
      verify(valueOperations).set(eq(expectedKey), eq(expectedJson), eq(Duration.ofHours(24)));
      assertThat(result).isEqualTo(game);
    }

    @Test
    @DisplayName("Should use custom TTL from subclass")
    void save_withCustomTtl_usesSubclassTtl() throws Exception {
      // Given
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      TestJsonRedisRepository customTtlRepo =
          new TestJsonRedisRepository(redisTemplate, objectMapper) {
            @Override
            protected Duration getTtl() {
              return Duration.ofHours(12);
            }
          };
      Game game = Game.createNew();
      String expectedKey = "game:" + game.getId();
      String expectedJson = objectMapper.writeValueAsString(game);

      // When
      customTtlRepo.save(game);

      // Then
      verify(valueOperations).set(eq(expectedKey), eq(expectedJson), eq(Duration.ofHours(12)));
    }
  }

  @Nested
  @DisplayName("findById() - Retrieve entity")
  class FindByIdTests {

    @Test
    @DisplayName("Should return entity when exists in Redis")
    void findById_whenEntityExists_returnsEntity() throws Exception {
      // Given
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      Game game = Game.createNew();
      String key = "game:" + game.getId();
      String json = objectMapper.writeValueAsString(game);
      when(valueOperations.get(key)).thenReturn(json);

      // When
      Optional<Game> result = repository.findById(game.getId());

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(game.getId());
    }

    @Test
    @DisplayName("Should return empty when entity does not exist")
    void findById_whenEntityDoesNotExist_returnsEmpty() {
      // Given
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      String gameId = "non-existent-id";
      String key = "game:" + gameId;
      when(valueOperations.get(key)).thenReturn(null);

      // When
      Optional<Game> result = repository.findById(gameId);

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("existsById() - Check existence")
  class ExistsByIdTests {

    @Test
    @DisplayName("Should return true when entity exists")
    void existsById_whenEntityExists_returnsTrue() {
      // Given
      String gameId = "game-123";
      String key = "game:" + gameId;
      when(redisTemplate.hasKey(key)).thenReturn(true);

      // When
      boolean result = repository.existsById(gameId);

      // Then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when entity does not exist")
    void existsById_whenEntityDoesNotExist_returnsFalse() {
      // Given
      String gameId = "game-123";
      String key = "game:" + gameId;
      when(redisTemplate.hasKey(key)).thenReturn(false);

      // When
      boolean result = repository.existsById(gameId);

      // Then
      assertThat(result).isFalse();
    }
  }

  @Nested
  @DisplayName("deleteById() - Remove entity")
  class DeleteByIdTests {

    @Test
    @DisplayName("Should delete entity from Redis")
    void deleteById_withValidId_deletesFromRedis() {
      // Given
      String gameId = "game-123";
      String key = "game:" + gameId;

      // When
      repository.deleteById(gameId);

      // Then
      verify(redisTemplate).delete(key);
    }
  }

  @Nested
  @DisplayName("Key building")
  class KeyBuildingTests {

    @Test
    @DisplayName("Should build correct Redis key with prefix")
    void buildKey_withPrefix_createsCorrectKey() throws Exception {
      // Given
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      Game game = Game.createNew();
      String expectedKey = "game:" + game.getId();

      // When
      repository.save(game);

      // Then
      verify(valueOperations).set(eq(expectedKey), anyString(), any(Duration.class));
    }
  }

  // Test implementation of abstract class
  private static class TestJsonRedisRepository extends JsonRedisRepository<Game> {

    public TestJsonRedisRepository(
        RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
      super(redisTemplate, objectMapper, Game.class);
    }

    @Override
    protected String getKeyPrefix() {
      return "game:";
    }

    @Override
    protected String getEntityId(Game entity) {
      return entity.getId();
    }
  }
}
