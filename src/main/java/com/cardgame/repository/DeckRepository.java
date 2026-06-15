package com.cardgame.repository;

import com.cardgame.model.entity.Deck;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DeckRepository extends JsonRedisRepository<Deck> {

  private static final String KEY_PREFIX = "deck:";

  public DeckRepository(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
    super(redisTemplate, objectMapper, Deck.class);
  }

  @Override
  protected String getKeyPrefix() {
    return KEY_PREFIX;
  }

  @Override
  protected String getEntityId(Deck entity) {
    return entity.getId();
  }
}
