package com.cardgame.repository;

import com.cardgame.model.entity.Deck;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeckRepository extends CrudRepository<Deck, String> {
  // CRUD methods are automatically provided:
  // - save(Deck deck)
  // - findById(String id)
  // - findAll()
  // - deleteById(String id)
  // - existsById(String id)
  // - count()
}
