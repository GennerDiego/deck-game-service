package com.cardgame.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Generic repository for storing entities as JSON strings in Redis.
 *
 * @param <T> Entity type
 */
@Slf4j
@RequiredArgsConstructor
public abstract class JsonRedisRepository<T> {

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  private final Class<T> entityType;

  protected abstract String getKeyPrefix();

  protected abstract String getEntityId(T entity);

  protected Duration getTtl() {
    return Duration.ofHours(24); // Default TTL: 24 hours
  }

  public T save(T entity) {
    String key = buildKey(getEntityId(entity));
    String json = toJson(entity);
    redisTemplate.opsForValue().set(key, json, getTtl());
    log.debug("Saved {} to Redis: {}", entityType.getSimpleName(), key);
    return entity;
  }

  public Optional<T> findById(String id) {
    String key = buildKey(id);
    String json = redisTemplate.opsForValue().get(key);
    if (json == null) {
      log.debug("{} not found in Redis: {}", entityType.getSimpleName(), key);
      return Optional.empty();
    }
    T entity = fromJson(json);
    log.debug("Loaded {} from Redis: {}", entityType.getSimpleName(), key);
    return Optional.of(entity);
  }

  public boolean existsById(String id) {
    String key = buildKey(id);
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

  public void deleteById(String id) {
    String key = buildKey(id);
    redisTemplate.delete(key);
    log.debug("Deleted {} from Redis: {}", entityType.getSimpleName(), key);
  }

  private String buildKey(String id) {
    return getKeyPrefix() + id;
  }

  @SneakyThrows
  private String toJson(T entity) {
    return objectMapper.writeValueAsString(entity);
  }

  @SneakyThrows
  private T fromJson(String json) {
    return objectMapper.readValue(json, entityType);
  }
}
