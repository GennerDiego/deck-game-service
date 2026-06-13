package com.cardgame.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerScoreResponse {
  private String playerId;
  private String name;
  private int totalValue;
  private int cardCount;

  public static PlayerScoreResponse of(
      String playerId, String name, int totalValue, int cardCount) {
    return new PlayerScoreResponse(playerId, name, totalValue, cardCount);
  }
}
