package com.cardgame.service;

import com.cardgame.model.entity.Deck;
import com.cardgame.repository.DeckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeckService {

  private static final Logger log = LoggerFactory.getLogger(DeckService.class);

  @Autowired private DeckRepository deckRepository;

  public Deck createDeck() {
    Deck deck = Deck.createNew();
    deck = deckRepository.save(deck);
    log.info("Deck created with ID: {} (52 cards)", deck.getId());
    return deck;
  }
}
