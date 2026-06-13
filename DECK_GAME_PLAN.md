# Implementation Plan - Deck of Cards Game

## 📋 Project Context

### Description
Develop a set of classes and a REST API that represent a poker-style deck of cards along with services for a basic game between multiple players.

### Definitions
- **Deck**: 52 cards divided into 4 suits (hearts, spades, clubs, diamonds) with values: Ace, 2-10, Jack, Queen, and King
- **Game Deck (Shoe)**: Collection of one or more decks added to a game
- **Player**: Game participant who receives cards

---

## 🎯 Functional Requirements

### 1. Game Management
- ✅ **Create a game** (POST /games)
- ✅ **Delete a game** (DELETE /games/{gameId})

### 2. Deck Management
- ✅ **Create a deck** (POST /decks)
- ✅ **Add deck to game deck** (POST /games/{gameId}/decks)
  - ⚠️ **RULE**: Once added, the deck CANNOT be removed

### 3. Player Management
- ✅ **Add player to game** (POST /games/{gameId}/players)
- ✅ **Remove player from game** (DELETE /games/{gameId}/players/{playerId})

### 4. Card Dealing
- ✅ **Deal cards to a player** (POST /games/{gameId}/players/{playerId}/deal)
  - **RULE**: For a game deck with 1 deck:
    - 1 shuffle + 52 calls to dealCards(1) = all 52 cards in random order
    - 53rd call = no card is dealt
  - **RULE**: Same behavior for game decks with multiple decks

### 5. Card Queries
- ✅ **List player's cards** (GET /games/{gameId}/players/{playerId}/cards)
- ✅ **List players with scores** (GET /games/{gameId}/players/scores)
  - Calculate total value of cards (face values only)
  - Sort in descending order (highest to lowest)
  - Example: Player A (10 + King = 23) comes before Player B (7 + Queen = 19)

### 6. Game Deck Statistics
- ✅ **Count undealt cards by suit** (GET /games/{gameId}/deck/suits-count)
  - Example: 5 hearts, 3 spades, etc.
- ✅ **Count each remaining card in deck** (GET /games/{gameId}/deck/cards-count)
  - Sorted by suit (hearts, spades, clubs, diamonds)
  - Sorted by value (King, Queen, Jack, 10...2, Ace with value 1)

### 7. Shuffling
- ✅ **Shuffle the game deck** (POST /games/{gameId}/deck/shuffle)
  - **RULE**: DO NOT use library-provided "shuffle" operations
  - **RULE**: MAY use library-provided random number generators
  - **RULE**: Can be called at any time
  - Returns no value, but randomly permutes the cards

---

## 🏗️ Architecture and Technologies

### Technology Stack
```
├── Language: Java 17+
├── Framework: Spring Boot 3.x
├── Cache/Memory: Redis
├── Build: Maven or Gradle
├── Logging: SLF4J + Logback
├── API Documentation: OpenAPI 3.0 (Swagger)
├── Testing: JUnit 5 + Mockito + Testcontainers
└── Containerization: Docker + Docker Compose
```

### Package Structure
```
com.cardgame
├── controller/          # REST Controllers
├── service/             # Business Logic
├── repository/          # Data Access (Redis)
├── model/               # Domain Models
│   ├── entity/          # Game, Deck, Card, Player
│   └── dto/             # Request/Response DTOs
├── config/              # Configuration (Redis, Swagger, etc.)
├── exception/           # Custom Exceptions
├── util/                # Utilities (Shuffle, etc.)
└── validator/           # Custom Validations
```

---

## 📐 Data Modeling

### Main Entities

#### Card
```java
- suit: Enum (HEARTS, SPADES, CLUBS, DIAMONDS)
- rank: Enum (ACE, TWO, THREE...KING)
- faceValue: int (1-13)
```

#### Deck
```java
- id: String (UUID)
- cards: List<Card> (52 cards)
- createdAt: Instant
```

#### Game
```java
- id: String (UUID)
- gameDeck: List<Card> (shoe)
- players: Map<String, Player>
- dealtCards: Map<String, List<Card>> (playerId -> cards)
- createdAt: Instant
```

#### Player
```java
- id: String (UUID)
- name: String
- gameId: String
```

### Redis Storage
```
Redis Key                            | Type      | Value
-------------------------------------|-----------|------------------
game:{gameId}                        | Hash      | Game JSON
game:{gameId}:players                | Hash      | Map<playerId, Player>
game:{gameId}:deck                   | List      | Cards JSON
game:{gameId}:dealt:{playerId}       | List      | Cards JSON
deck:{deckId}                        | Hash      | Deck JSON
```

---

## 🔄 Operation Flows

### Flow 1: Create and Configure Game
```
1. POST /games → Create empty game
2. POST /decks → Create standard deck (52 cards)
3. POST /games/{gameId}/decks → Add deck to game deck
4. POST /games/{gameId}/players → Add player(s)
5. POST /games/{gameId}/deck/shuffle → Shuffle the game deck
```

### Flow 2: Playing
```
1. POST /games/{gameId}/players/{playerId}/deal?count=5 → Deal 5 cards
2. GET /games/{gameId}/players/{playerId}/cards → View player's cards
3. GET /games/{gameId}/players/scores → View player rankings
4. GET /games/{gameId}/deck/suits-count → View remaining cards
```

### Flow 3: Finish
```
1. GET /games/{gameId}/players/scores → View final results
2. DELETE /games/{gameId} → Delete game
```

---

## 🛡️ Business Rules

### BR001 - Deck Creation
- A standard deck always has 52 cards
- 4 suits × 13 ranks = 52 cards
- Cannot have duplicate cards in a single deck

### BR002 - Adding Deck to Game
- A deck can be added multiple times to the game deck
- Once added, it cannot be removed
- Game deck = concatenation of all added decks

### BR003 - Card Dealing
- Can only deal cards if there are cards available in the game deck
- Dealt cards are removed from the game deck
- If trying to deal more cards than available, return only available cards
- Dealt cards are associated with the player

### BR004 - Score Calculation
- Face values: Ace=1, 2-10=numeric value, Jack=11, Queen=12, King=13
- Score = sum of face values of all player's cards

### BR005 - Shuffling
- Use Fisher-Yates algorithm (or similar) implemented manually
- Can be called at any time
- Does not affect already dealt cards, only remaining ones in game deck
- Use Java's Random (allowed)

### BR006 - Validations
- Cannot add player with same ID
- Cannot deal cards to non-existent player
- Cannot delete game with active players (optional: allow with warning)

---

## 🧪 Testing Strategy

### Unit Tests (JUnit 5 + Mockito)
```
✅ CardTest - Validate creation and properties
✅ DeckTest - Validate 52 unique cards
✅ ShuffleUtilTest - Validate shuffle algorithm
✅ GameServiceTest - Business logic
✅ PlayerServiceTest - Player management
```

### Integration Tests (Testcontainers)
```
✅ GameControllerIT - REST endpoints
✅ RedisIntegrationIT - Redis persistence
✅ CompleteGameFlowIT - Complete end-to-end flow
```

### Load Tests (Optional - JMeter/Gatling)
```
✅ 100 concurrent games
✅ 1000 card deals/second
```

---

## 📊 Logging and Monitoring

### Logging Strategy
```java
// Log Levels
TRACE - Shuffle details
DEBUG - Card dealing operations
INFO  - Game/player creation/deletion
WARN  - Invalid operation attempts
ERROR - Redis failures, unhandled exceptions
```

### Structured Logs (JSON)
```json
{
  "timestamp": "2026-06-12T10:30:00Z",
  "level": "INFO",
  "logger": "GameService",
  "message": "Game created",
  "gameId": "uuid-123",
  "deckCount": 2,
  "playerCount": 0
}
```

### Metrics (Micrometer + Prometheus)
```
- Counter: games_created_total
- Counter: cards_dealt_total
- Gauge: active_games
- Histogram: deal_duration_seconds
```

---

## 🔒 Security and Best Practices

### Validations
- ✅ Validate input UUIDs
- ✅ Validate quantities (count > 0)
- ✅ Validate resource existence before operations
- ✅ Rate limiting (optional)

### Error Handling
```
400 Bad Request - Invalid data
404 Not Found - Resource not found
409 Conflict - Conflicting operation (e.g., duplicate player)
500 Internal Server Error - Unexpected errors
```

### API Documentation
- OpenAPI 3.0 with Swagger UI
- Request/response examples
- Possible error descriptions

---

## 🚀 Implementation Phases

### Phase 1: Initial Setup (2-3 days)
- [ ] Setup Spring Boot project
- [ ] Configure Redis (Docker Compose)
- [ ] Configure logging (Logback)
- [ ] Configure Swagger
- [ ] Package structure

### Phase 2: Domain Model (2 days)
- [ ] Implement Card (Suit, Rank enums)
- [ ] Implement Deck
- [ ] Implement Game
- [ ] Implement Player
- [ ] Request/Response DTOs

### Phase 3: Persistence Layer (2 days)
- [ ] RedisConfig
- [ ] GameRepository (RedisTemplate)
- [ ] DeckRepository
- [ ] JSON Serialization/Deserialization

### Phase 4: Business Logic (3-4 days)
- [ ] GameService (create, delete, shuffle)
- [ ] DeckService (create, add to game)
- [ ] PlayerService (add, remove)
- [ ] DealService (deal cards)
- [ ] ScoreService (calculate scores)
- [ ] ShuffleUtil (manual algorithm)

### Phase 5: REST API (2 days)
- [ ] GameController
- [ ] PlayerController
- [ ] DeckController
- [ ] Global exception handlers

### Phase 6: Testing (3 days)
- [ ] Unit tests (70%+ coverage)
- [ ] Integration tests
- [ ] Complete flow test

### Phase 7: Documentation and Deploy (1-2 days)
- [ ] Complete README
- [ ] Swagger documentation
- [ ] Final Docker Compose
- [ ] Initialization scripts

**Total Estimated: 15-19 days**

---

## 📦 Deliverables

1. ✅ Complete Java source code
2. ✅ Dockerfile + Docker Compose
3. ✅ README with execution instructions
4. ✅ Accessible Swagger UI (e.g., http://localhost:8080/swagger-ui.html)
5. ✅ Executable automated tests (mvn test)
6. ✅ Postman/Insomnia collection (optional)
7. ✅ Architectural decision documentation

---

## 💡 Additional Suggestions (Beyond Requirements)

### Recommended Extra Features
1. **Audit**: Log all actions (who did what and when)
2. **Persistence**: Besides Redis, add PostgreSQL for game history
3. **WebSocket**: Real-time card dealing notifications
4. **Undo**: Functionality to undo last deal
5. **Statistics**: Endpoint with aggregated game statistics
6. **Export**: Export game state to JSON/CSV
7. **Rule Validation**: Configure custom game rules
8. **Multi-tenancy**: Support for multiple tenants/organizations
9. **Cache**: Cache for frequent queries (Redis Cache)
10. **Health Check**: /actuator/health endpoints for monitoring

### Technical Improvements
1. **CI/CD**: GitLab/GitHub Actions pipeline
2. **Observability**: Integration with ELK Stack or Grafana
3. **API Versioning**: /v1/games, /v2/games
4. **Rate Limiting**: Protection against API abuse
5. **CORS**: Configuration for frontend
6. **Compression**: Enable GZIP
7. **Pagination**: For large listings
8. **Filters**: Query params to filter games (status, date, etc.)

---

## 🎲 Shuffle Algorithm (Fisher-Yates)

```java
/**
 * Manual implementation of Fisher-Yates algorithm
 * Complexity: O(n)
 * 
 * @param cards List of cards to shuffle (modified in-place)
 */
public void shuffle(List<Card> cards) {
    Random random = new Random();
    
    // Iterate backwards
    for (int i = cards.size() - 1; i > 0; i--) {
        // Choose random index from 0 to i (inclusive)
        int j = random.nextInt(i + 1);
        
        // Swap elements i and j
        Card temp = cards.get(i);
        cards.set(i, cards.get(j));
        cards.set(j, temp);
    }
    
    log.debug("Deck shuffled with {} cards", cards.size());
}
```

---

## 📝 API Usage Examples

### Example 1: Create and Play
```bash
# 1. Create game
POST /api/v1/games
Response: { "gameId": "game-123" }

# 2. Create deck
POST /api/v1/decks
Response: { "deckId": "deck-456" }

# 3. Add deck to game
POST /api/v1/games/game-123/decks
Body: { "deckId": "deck-456" }

# 4. Add players
POST /api/v1/games/game-123/players
Body: { "name": "Alice" }
Response: { "playerId": "player-789" }

# 5. Shuffle
POST /api/v1/games/game-123/deck/shuffle

# 6. Deal 5 cards to Alice
POST /api/v1/games/game-123/players/player-789/deal?count=5
Response: { "cardsDealt": 5, "cardsRemaining": 47 }

# 7. View Alice's cards
GET /api/v1/games/game-123/players/player-789/cards
Response: {
  "playerId": "player-789",
  "cards": [
    { "suit": "HEARTS", "rank": "KING", "faceValue": 13 },
    { "suit": "SPADES", "rank": "ACE", "faceValue": 1 },
    ...
  ]
}

# 8. View rankings
GET /api/v1/games/game-123/players/scores
Response: {
  "players": [
    { "playerId": "player-789", "name": "Alice", "totalValue": 45, "cardCount": 5 }
  ]
}
```

---

## ⚠️ Architectural Decisions and Trade-offs

### Decision 1: Redis as Primary Store
**Choice**: Use Redis as primary storage (not just cache)
**Pros**: 
- Excellent read/write performance
- Native data structures (List, Hash)
- Automatic TTL for old game cleanup
**Cons**:
- No long-term persistence (mitigation: RDB/AOF)
- Memory limit

### Decision 2: REST over WebSocket
**Choice**: Synchronous REST API
**Pros**: 
- Simpler to implement and test
- Better for current requirements
- Stateless
**Cons**:
- No real-time notifications (can add later)

### Decision 3: Cards in Game Deck (not reference)
**Choice**: Copy cards to game deck (don't maintain reference)
**Pros**: 
- Immutability of original deck
- Isolation between games
**Cons**:
- More memory usage (acceptable for current scale)

### Decision 4: No Authentication (Phase 1)
**Choice**: No authentication in initial version
**Pros**: 
- Focus on functional requirements
- Faster to implement
**Cons**:
- Not production-ready (add Spring Security later)

---

## 📚 References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Fisher-Yates Shuffle Algorithm](https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Redis Best Practices](https://redis.io/docs/manual/patterns/)

---

## ✅ Final Checklist

### Before Delivery
- [ ] All tests passing (mvn test)
- [ ] Test coverage > 70%
- [ ] Swagger documented and accessible
- [ ] README updated with instructions
- [ ] Docker Compose functional
- [ ] Structured logging implemented
- [ ] Global error handling
- [ ] Internal code review performed
- [ ] Performance tested (100+ concurrent games)
- [ ] No hardcoded values (use application.yml)

---

**Document created**: 2026-06-12  
**Last update**: 2026-06-12  
**Version**: 1.0