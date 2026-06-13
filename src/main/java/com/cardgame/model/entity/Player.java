package com.cardgame.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_NULL)
@RedisHash("players")
public class Player implements Serializable {

  @Id private String id;
  private String name;
  private String gameId;

  @Builder.Default private List<Card> cards = new ArrayList<>();

  public static Player createNew(String name, String gameId) {
    return Player.builder()
        .id(UUID.randomUUID().toString())
        .name(name)
        .gameId(gameId)
        .cards(new ArrayList<>())
        .build();
  }

  public int getTotalValue() {
    return cards.stream().mapToInt(Card::getFaceValue).sum();
  }

  public int getCardCount() {
    return cards.size();
  }
}
