package com.cardgame.repository;

import com.cardgame.model.entity.Game;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GameRepository extends JsonRedisRepository<Game> {

  public GameRepository(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
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
