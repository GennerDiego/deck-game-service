package com.cardgame.model.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card implements Serializable {

  private Suit suit;
  private Rank rank;

  public int getFaceValue() {
    return rank.getFaceValue();
  }

  @Override
  public String toString() {
    return rank + " of " + suit;
  }
}
