package com.cardgame.repository;

import com.cardgame.model.entity.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends CrudRepository<Game, String> {
  // CRUD methods are automatically provided:
  // - save(Game game)
  // - findById(String id)
  // - findAll()
  // - deleteById(String id)
  // - existsById(String id)
  // - count()
}
