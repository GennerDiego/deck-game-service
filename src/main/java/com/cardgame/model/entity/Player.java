package com.cardgame.model.entity;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player implements Serializable {

  private String id;
  private String name;
  private String gameId;

  public static Player createNew(String name, String gameId) {
    return new Player(UUID.randomUUID().toString(), name, gameId);
  }
}
