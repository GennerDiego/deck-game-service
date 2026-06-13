# Integration Tests

## 📋 Overview

Integration tests configurados com:
- **Spring Boot Test** - Sobe a aplicação completa
- **Testcontainers** - Redis real em container Docker
- **TestRestTemplate** - Chamadas HTTP reais aos endpoints
- **AssertJ** - Assertions fluentes e legíveis

## 🏗️ Estrutura

```
src/test/java/com/cardgame/
├── AbstractIntegrationTest.java                      # Base with Testcontainers setup
└── integration/                                      # Integration tests package
    ├── BaseIntegrationTest.java                      # Shared helper methods (144 lines)
    ├── GameManagementIntegrationTest.java            # Game CRUD operations (99 lines)
    ├── DeckManagementIntegrationTest.java            # Deck & Shuffle operations (144 lines)
    ├── PlayerManagementIntegrationTest.java          # Player Add/Remove (111 lines)
    ├── DealCardsIntegrationTest.java                 # Deal Cards - CORE (247 lines)
    ├── QueryOperationsIntegrationTest.java           # Scores, Counts, Cards (313 lines)
    └── RealisticGameFlowIntegrationTest.java         # Complete game flows (388 lines)
```

**Total:** 1,446 lines split across 7 focused test files (was 1,605 lines in 1 file)

## 🧪 Testes Implementados

### Integration Tests (56+ tests across 7 files)

**Modular structure** - Each file focuses on a specific domain:

#### 1️⃣ **GameManagementIntegrationTest** (5 tests)
- ✅ Create game with valid ID and initial state
- ✅ Get game details when game exists
- ✅ Return 404 when getting non-existent game
- ✅ Delete game successfully and verify removal
- ✅ Return 404 when deleting non-existent game

#### 2️⃣ **DeckManagementIntegrationTest** (7 tests)
- ✅ Add first deck with 52 cards
- ✅ Add multiple decks creating a shoe (52, 104, 156, 208 cards)
- ✅ Return 404 when adding deck to non-existent game
- ✅ Verify deck contains all 52 unique cards (4 suits × 13 ranks)
- ✅ Do not throw exception when shuffling empty deck
- ✅ Randomize card order when shuffling deck with cards
- ✅ Allow shuffle to be called multiple times

#### 3️⃣ **PlayerManagementIntegrationTest** (5 tests)
- ✅ Add first player to game successfully
- ✅ Add multiple players to game
- ✅ Remove player and move cards to discard pile
- ✅ Return 404 when removing non-existent player
- ✅ Return 404 when adding player to non-existent game

#### 4️⃣ **DealCardsIntegrationTest - CORE REQUIREMENTS** (10 tests)
- ✅ Deal single card successfully
- ✅ Deal multiple cards at once
- ✅ **[CORE]** Deal all 52 cards in random order after shuffle
- ✅ **[CORE]** Return empty list on 53rd deal from empty deck
- ✅ Deal only available cards when requesting more than available
- ✅ Return 400 when dealing with invalid count (zero/negative)
- ✅ Return 404 when dealing to non-existent player
- ✅ Deal from multiple decks (shoe) successfully
- ✅ Shuffle only remaining cards after dealing

#### 5️⃣ **QueryOperationsIntegrationTest** (16 tests across 4 nested classes)
**5.1 Player Scores - CORE** (3 tests)
- ✅ **[CORE]** Return players sorted by score descending
- ✅ Return empty list when game has no players
- ✅ **[CORE]** Calculate scores using correct face values (Ace=1, Jack=11, Queen=12, King=13)

**5.2 Suit Counts Query** (4 tests)
- ✅ Return 13 per suit for full deck
- ✅ Return correct counts after dealing cards
- ✅ Return all zeros when deck is empty
- ✅ Return combined counts for multiple decks

**5.3 Card Counts - CORE** (4 tests)
- ✅ **[CORE]** Return sorted card counts for full deck
- ✅ Show duplicate counts for multiple decks
- ✅ Reflect remaining cards after partial dealing
- ✅ Return empty map when deck is empty

**5.4 Player Cards Query** (3 tests)
- ✅ Return all cards for player with cards
- ✅ Return empty list for player with no cards
- ✅ Return 404 for non-existent player

#### 6️⃣ **RealisticGameFlowIntegrationTest** (6 tests) ⭐
- ✅ **Poker Game Flow** - Single deck, 4 players, deal 5 cards each, verify scores
- ✅ **Blackjack Flow** - 6-deck shoe, 3 players, multiple deal rounds, shuffle mid-game
- ✅ **Complete Lifecycle** - Create, play multiple rounds, player leaves, cleanup
- ✅ **Draw All Cards** - Deal until deck exhausted, handle empty deck
- ✅ **Progressive Shuffle** - Shuffle at different game stages (empty, full, partial)
- ✅ **Query Operations Mid-Game** - Verify all queries during active game

## 🚀 Como Executar

### Pré-requisitos
- Docker rodando (para Testcontainers)
- Java 17+

### Executar todos os testes
```bash
./gradlew test
```

### Executar apenas Integration Tests
```bash
./gradlew test --tests "*IntegrationTest"
```

### Executar teste específico
```bash
# Todos os testes de integração
./gradlew test --tests "GameIntegrationTest"

# Teste específico
./gradlew test --tests "GameIntegrationTest.completeGameFlow_shouldWorkEndToEnd"

# Somente testes de deck business rules
./gradlew test --tests "GameIntegrationTest.*Deck*"
```

### Com logs detalhados
```bash
./gradlew test --info
```

### Com coverage report
```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## 📊 Cenários de Teste Detalhados

### ✅ [CORE] Cenário 1: Shuffle → Deal 52 Cards → 53rd Deal Returns Empty
```java
// Core Requirement from Spec
1. Create game
2. Add 1 deck (52 cards)
3. Shuffle deck
4. Loop 52 times: dealCards(gameId, playerId, 1)
   → Each call returns 1 card
   → Player receives all 52 unique cards in random order
5. 53rd call: dealCards(gameId, playerId, 1)
   → Returns empty list []
   → cardsRemaining = 0
   → Player still has 52 cards (unchanged)

✅ Test: dealCards_afterShuffle_dealsAll52CardsInRandomOrder
✅ Test: dealCards_whenDeckIsEmpty_returnsNoCards
```

### ✅ [CORE] Cenário 2: Player Scores Sorted Descending
```java
// Core Requirement: Sort by total value (high to low)
Given:
  - Player A: 10♠ + King♥ → totalValue = 23
  - Player B: 7♦ + Queen♣ → totalValue = 19
  - Player C: Ace♠ + 2♥ → totalValue = 3

When: GET /games/{id}/players/scores

Then: Response order:
  [
    {playerId: "A", name: "Alice", totalValue: 23, cardCount: 2},
    {playerId: "B", name: "Bob", totalValue: 19, cardCount: 2},
    {playerId: "C", name: "Charlie", totalValue: 3, cardCount: 2}
  ]

✅ Test: getPlayerScores_withMultiplePlayers_returnsSortedByTotalValueDescending
✅ Test: getPlayerScores_usesCorrectFaceValues (Ace=1, Jack=11, Queen=12, King=13)
```

### ✅ [CORE] Cenário 3: Card Counts Sorted by Suit and Value
```java
// Core Requirement: Sort by Suit (H, S, C, D) then Value (K→A)
GET /games/{id}/deck/cards-count

Response order (high to low per suit):
{
  "ACE of HEARTS": 1,
  "TWO of HEARTS": 1,
  ...
  "KING of HEARTS": 1,
  "ACE of SPADES": 1,
  ...
  "ACE of DIAMONDS": 1
}

With 3 decks: Each card count = 3
After dealing: Reflects only remaining cards

✅ Test: getCardCounts_withFullDeck_returnsSortedBySuitAndValue
✅ Test: getCardCounts_withMultipleDecks_showsDuplicateCounts
```

### ✅ Cenário 4: Complete End-to-End Game Flow
```java
1. POST /games → Create game
2. POST /games/{id}/decks (2x) → Add 2 decks (104 cards)
3. POST /games/{id}/deck/shuffle → Shuffle
4. POST /games/{id}/players (3x) → Add Alice, Bob, Charlie
5. POST /games/{id}/players/{id}/deal?count=10 → Deal to Alice
6. POST /games/{id}/players/{id}/deal?count=15 → Deal to Bob
7. POST /games/{id}/players/{id}/deal?count=20 → Deal to Charlie
8. GET /games/{id}/players/scores → Get sorted scores
9. GET /games/{id}/deck/suits-count → Remaining: 59 cards
10. DELETE /games/{id}/players/{charlieId} → Remove Charlie (20 cards → discard)
11. DELETE /games/{id} → Delete game

✅ Test: completeGameFlow_fromCreationToCompletion_allOperationsSucceed
```

### ✅ Cenário 5: Business Rule - Deck Cannot Be Removed
```java
// Once a deck is added, it CANNOT be removed
POST /games/{id}/decks → 200 OK (52 cards)
POST /games/{id}/decks → 200 OK (104 cards total)
POST /games/{id}/decks → 200 OK (156 cards total)

// No DELETE endpoint exists for decks
DELETE /games/{id}/decks/{deckId} → 404 Not Found (endpoint doesn't exist)

Note: This is validated in the API design (no delete endpoint)
```

### ✅ Cenário 6: Shuffle Only Affects Remaining Cards
```java
1. Create game + Add deck (52 cards)
2. Add player "Alice"
3. Deal 10 cards to Alice → 42 cards remaining
4. Shuffle deck

Result:
  - Alice still has the same 10 cards (unchanged)
  - Only the 42 remaining cards in deck are shuffled
  - cardsRemaining = 42

✅ Test: shuffleGameDeck_afterDealingCards_shufflesOnlyRemainingCards
```

### ✅ Cenário 7: Multiple Players - No Duplicate Cards
```java
1. Create game + Add 2 decks (104 cards)
2. Add 3 players: Alice, Bob, Charlie
3. Deal 20 cards to Alice
4. Deal 30 cards to Bob
5. Deal 25 cards to Charlie

Validation:
  - Total dealt: 75 cards
  - All 75 cards are unique (no duplicates between players)
  - cardsRemaining = 29 (104 - 75)

✅ Test: multiplePlayers_dealingFromSameDeck_maintainsConsistency
```

### ✅ Cenário 8: Persistence - totalDecksAdded Preserved
```java
// This was a bug fix - totalDecksAdded must be persisted
1. Create game
2. Add 3 decks
3. Deal 30 cards to player
4. Fetch game from Redis

Validation:
  - totalDecksAdded = 3 (preserved in Redis)
  - totalCardsInDeck = 156 (3 × 52)
  - cardsRemaining = 126 (156 - 30)

✅ Test: gamePersistence_afterMultipleOperations_dataIsPreserved
```

---

## 🎮 Cenários de Jogo Realistas (NEW)

### ✅ Cenário 9: Poker Game Flow - Texas Hold'em Style
```java
// Realistic poker game with 4 players
1. Create game → 0 cards
2. Add 1 deck → 52 cards
3. Add 4 players → Alice, Bob, Charlie, Diana
4. Shuffle deck → Randomize cards
5. Deal 5 cards to each player → 20 cards dealt
6. Get player scores → All 4 players with scores sorted descending
7. Get suit counts → 32 cards remaining (52 - 20)

Validations:
  - Each player has exactly 5 cards
  - No duplicate cards between players
  - cardsRemaining = 32
  - Scores sorted by totalValue (high to low)
  - All suits represented in remaining cards

✅ Test: pokerGameFlow_singleDeck_fourPlayers_fiveCardsEach
```

### ✅ Cenário 10: Blackjack Flow - Casino 6-Deck Shoe
```java
// Casino blackjack with multiple decks and mid-game shuffle
1. Create game
2. Add 6 decks → 312 cards (shoe)
3. Shuffle shoe
4. Add 3 players
5. Round 1: Deal 2 cards to each → 6 cards dealt
   → cardsRemaining = 306
6. Round 2: Player1 hits (1 card), Player2 hits twice (2 cards)
   → Player1 has 3 cards, Player2 has 4 cards, Player3 has 2 cards
   → cardsRemaining = 303
7. Shuffle mid-game → Only remaining 303 cards shuffled
   → Dealt cards UNCHANGED
8. Round 3: Player3 hits 3 times → Player3 has 5 cards
   → cardsRemaining = 300

Validations:
  - Shoe starts with 312 cards (6 × 52)
  - Mid-game shuffle doesn't affect already dealt cards
  - Total cards dealt = 12 (3 + 4 + 5)
  - cardsRemaining = 300 (312 - 12)

✅ Test: blackjackGameFlow_multipleDecks_dealRounds_shuffleMidGame
```

### ✅ Cenário 11: Complete Game Lifecycle
```java
// Full game from creation to cleanup with player leaving
1. Create game
2. Add 2 decks → 104 cards
3. Add 3 players → Alice, Bob, Charlie
4. Shuffle
5. Round 1: Deal 3 cards to each → 9 cards dealt
6. Round 2: Deal 2 more to each → 6 cards dealt
   → Each player has 5 cards
7. Get scores → All 3 players
8. Charlie leaves → Remove player
   → Charlie's 5 cards move to discard pile
   → Only 2 players remain
9. Round 3: Deal 2 cards to Alice and Bob → 4 cards dealt
   → Alice has 7 cards, Bob has 7 cards
10. Final scores → Only 2 players
11. Delete game → Cleanup

Validations:
  - After player leaves: discardedCards = 5
  - cardsRemaining = 85 (104 - 5 dealt to Charlie - 14 to others)
  - Scores update correctly after player removal
  - Game deleted successfully (404 on GET)

✅ Test: completeGameLifecycle_createPlayPlayerLeavesCleanup
```

### ✅ Cenário 12: Draw All Cards - Deck Exhaustion
```java
// Deal until deck completely exhausted
1. Create game + Add 1 deck (52 cards)
2. Add 2 players → Alice, Bob
3. Shuffle
4. Alice deals 30 cards → Gets 30 cards
5. Bob tries to deal 30 cards → Gets only 22 (all remaining)
   → cardsRemaining = 0
6. Alice tries to deal 1 more → Returns empty list []
7. Verify final state:
   → Alice has 30 cards
   → Bob has 22 cards
   → Total = 52 cards (all deck cards)
   → Suit counts all zeros
   → Card counts empty map
   → Scores still work correctly

✅ Test: drawAllCardsScenario_dealUntilDeckExhausted
```

### ✅ Cenário 13: Progressive Shuffle at Different Stages
```java
// Shuffle called at various game stages
1. Create game
2. Shuffle 1 → Empty deck (should work without error)
3. Add 3 decks → 156 cards
4. Shuffle 2 → Full shoe
5. Deal 50 cards → cardsRemaining = 106
6. Shuffle 3 → Partial deck (only 106 cards shuffled)
7. Deal 50 more → cardsRemaining = 56
8. Shuffle 4 → Even fewer cards
9. Deal rest (100 requested) → Gets only 56
10. Shuffle 5 → Empty deck again

Validations:
  - Shuffle works at any stage
  - Dealt cards never affected by shuffle
  - All 156 cards eventually dealt
  - Player has all 156 cards at end

✅ Test: progressiveShuffle_shuffleAtDifferentStages
```

### ✅ Cenário 14: Query Operations During Active Game
```java
// Verify all query endpoints work correctly mid-game
1. Create game + Add 2 decks (104 cards)
2. Add 3 players + Shuffle
3. Deal initial cards: Alice=7, Bob=5, Charlie=3

Query Round 1:
  → Player scores: 3 players with correct card counts
  → Suit counts: 89 cards remaining
  → Card counts: 89 cards total

4. Deal more: Alice +3, Bob +5

Query Round 2:
  → Scores updated: Alice=10, Bob=10, Charlie=3
  → Suit counts: 81 cards remaining

5. Remove Charlie

Query Round 3:
  → Scores: Only 2 players
  → Game details: discardedCards = 3, players = 2

Validations:
  - All queries work during active game
  - Data consistency across operations
  - Scores update correctly
  - Counts accurate after each operation

✅ Test: queryOperationsMidGame_verifyQueriesDuringActiveGame
```

## 🐛 Troubleshooting

### Docker não está rodando
```
Error: Could not start container
Solution: Inicie o Docker Desktop
```

### Porta já em uso
```
Error: Port already in use
Solution: Os testes usam porta aleatória (@LocalServerPort)
```

### Redis container não sobe
```bash
# Verificar Docker
docker ps

# Limpar containers antigos
docker system prune -f
```

## 📝 Notas

- **Testcontainers** gerencia automaticamente o ciclo de vida do Redis
- Cada teste é **isolado** (setup/teardown automático)
- Todos os testes chamam **APIs HTTP reais** (end-to-end)
- Validam **status codes**, **response bodies**, e **business rules**
- **Total: 56+ integration tests** organizados em 11 módulos
- **4 Core Requirements** completamente testados:
- **6 Realistic Game Flow scenarios** simulando jogos reais
  - Poker (Texas Hold'em)
  - Blackjack (6-deck shoe)
  - Complete lifecycle com player leaving
  - Deck exhaustion
  - Progressive shuffle
  - Mid-game queries

### Core Requirements Testados
  1. ✅ Shuffle → Deal 52 → Deal 53rd returns empty
  2. ✅ Player scores sorted descending by totalValue
  3. ✅ Card counts sorted by suit and value (high to low)
  4. ✅ Face values: Ace=1, 2-10=numeric, Jack=11, Queen=12, King=13

## 🎯 Cobertura de Requisitos

### Core Requirements (da especificação original)
| Requirement | Test Coverage | Status |
|------------|---------------|--------|
| Create/Delete game | 5 tests | ✅ |
| Create deck (52 cards) | 4 tests | ✅ |
| Add deck to game (cannot be removed) | 4 tests | ✅ |
| Add/Remove players | 6 tests | ✅ |
| Deal cards (52 deals + 53rd empty) | 8 tests | ✅ **CORE** |
| Get player cards | 3 tests | ✅ |
| Get player scores (sorted desc) | 4 tests | ✅ **CORE** |
| Suit counts (undealt cards) | 4 tests | ✅ |
| Card counts (sorted by suit/value) | 4 tests | ✅ **CORE** |
| Shuffle deck (anytime, manual algorithm) | 4 tests | ✅ |

### Edge Cases & Business Rules
- ✅ Empty deck operations (shuffle, deal, counts)
- ✅ Multiple decks (shoe: 104, 156, 208 cards)
- ✅ Deal more than available → returns partial
- ✅ Shuffle only affects remaining cards
- ✅ Player removal moves cards to discard
- ✅ No duplicate cards between players
- ✅ Persistence validation (totalDecksAdded)
- ✅ 404 Not Found for invalid IDs
- ✅ 400 Bad Request for invalid inputs
- ✅ 409 Conflict for duplicate players

## 🔍 Como Verificar Cobertura

```bash
# Run tests with coverage
./gradlew test jacocoTestReport

# Open coverage report
open build/reports/jacoco/test/html/index.html

# Target: 70%+ code coverage
# Current focus: 100% coverage for:
#   - ShuffleUtil (Fisher-Yates algorithm)
#   - Score calculation
#   - Core business rules
```
