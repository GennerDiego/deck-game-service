package com.cardgame.repository;

import com.cardgame.model.entity.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends CrudRepository<Player, String> {
  // CRUD methods are automatically provided:
  // - save(Player player)
  // - findById(String id)
  // - findAll()
  // - deleteById(String id)
  // - existsById(String id)
  // - count()
}
