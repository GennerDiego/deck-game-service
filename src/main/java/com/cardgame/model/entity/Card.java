package com.cardgame.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_NULL)
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
