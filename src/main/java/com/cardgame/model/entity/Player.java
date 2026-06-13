package com.cardgame.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_NULL)
@JsonPropertyOrder({"id", "name", "cards"})
public class Player implements Serializable {

  private String id;
  private String name;

  @Builder.Default private List<Card> cards = new ArrayList<>();

  public static Player createNew(String name) {
    return Player.builder()
        .id(UUID.randomUUID().toString())
        .name(name)
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
