# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot REST API service that implements a poker-style deck of cards game system with Redis as the primary data store. The service allows creating games, managing decks and players, dealing cards, and tracking scores.

**Status**: Project is in planning phase. Implementation has not yet started.

## Technology Stack

- **Language**: Java 17+
- **Framework**: Spring Boot 3.x
- **Primary Storage**: Redis
- **Build Tool**: Gradle 8.8 (Kotlin DSL)
- **Testing**: JUnit 5, Mockito, Testcontainers
- **API Documentation**: OpenAPI 3.0 (Swagger)
- **Containerization**: Docker + Docker Compose

## Project Structure (Planned)

```
com.cardgame
├── controller/          # REST API endpoints
├── service/             # Business logic layer
├── repository/          # Redis data access
├── model/
│   ├── entity/          # Game, Deck, Card, Player domain models
│   └── dto/             # API request/response objects
├── config/              # Spring configuration (Redis, Swagger, etc.)
├── exception/           # Custom exceptions and global handlers
├── util/                # Shuffle algorithm and other utilities
└── validator/           # Input validation logic
```

## Core Domain Rules

### Card and Deck Rules
- A standard deck has exactly 52 cards (4 suits × 13 ranks)
- Suits: HEARTS, SPADES, CLUBS, DIAMONDS
- Ranks: ACE, TWO, THREE...KING
- Face values: Ace=1, 2-10=numeric value, Jack=11, Queen=12, King=13

### Game Rules
- **Multiple decks can be added** to create a "game deck" (shoe): 1 deck = 52 cards, 2 decks = 104 cards, etc.
- **Decks CANNOT be removed** once added to the game
- Dealt cards are removed from the game deck and associated with the player
- If requesting more cards than available in the shoe, deal only available cards
- Total cards in shoe = sum of all decks added (e.g., 3 decks = 156 cards)

### Shuffling Requirements
- **MUST implement shuffle algorithm manually** (e.g., Fisher-Yates)
- **DO NOT use library-provided shuffle operations**
- MAY use library-provided random number generators (Java's Random)
- Shuffle can be called at any time
- Shuffle only affects remaining cards in game deck, not already dealt cards

### Score Calculation
- Player score = sum of face values of all cards held
- Sort players by score in descending order (highest to lowest)

## Redis Data Model

```
Redis Key Structure:
- game:{gameId}                    → Hash (Game JSON)
- game:{gameId}:players            → Hash (Map<playerId, Player>)
- game:{gameId}:deck               → List (Cards JSON)
- game:{gameId}:dealt:{playerId}   → List (Cards JSON)
- deck:{deckId}                    → Hash (Deck JSON)
```

## API Endpoints (Planned)

### Game Management
- `POST /games` - Create a game
- `DELETE /games/{gameId}` - Delete a game

### Deck Management
- `POST /decks` - Create a standard 52-card deck
- `POST /games/{gameId}/decks` - Add deck to game's shoe
- `POST /games/{gameId}/deck/shuffle` - Shuffle the game deck

### Player Management
- `POST /games/{gameId}/players` - Add player to game
- `DELETE /games/{gameId}/players/{playerId}` - Remove player

### Card Operations
- `POST /games/{gameId}/players/{playerId}/deal?count=N` - Deal N cards to player
- `GET /games/{gameId}/players/{playerId}/cards` - List player's cards
- `GET /games/{gameId}/players/scores` - List all players with scores (sorted)

### Game Deck Queries
- `GET /games/{gameId}/deck/suits-count` - Count undealt cards by suit
- `GET /games/{gameId}/deck/cards-count` - Count each remaining card (sorted by suit then value)

## Development Workflow Rules

### Claude Code Skills Usage

When working in this repository, the following skills should be used:

- **`code-review`**: Use before commits for automated code review (run with `/code-review` or let Claude invoke automatically)
- **`verify`**: Use after implementing features to test in running application
- **`run`**: Use to start the application and verify changes work correctly

**Note**: This project does not use `tm:coding-standards` or other organizational plugins. Follow the standards defined in this file instead.

### Code Formatting - Spotless
**MANDATORY**: Always run Spotless before creating commits.

```bash
# Maven
mvn spotless:apply

# Gradle
./gradlew spotlessApply
```

**When working with Claude Code**: This will be executed automatically before every commit.

**For other developers**: Configure pre-commit hook or run manually before committing.

### Pre-Commit Checklist
Before every commit, ensure:
1. ✅ Spotless has been applied (`mvn spotless:apply` or `./gradlew spotlessApply`)
2. ✅ All tests pass (`mvn test` or `./gradlew test`)
3. ✅ No compile errors
4. ✅ No unused imports or variables

## Coding Standards

### 1. Naming Conventions (Java/Spring Boot)

**Classes and Interfaces:**
- Controllers: `*Controller` suffix (e.g., `GameController`, `PlayerController`)
- Services: `*Service` suffix (e.g., `GameService`, `DeckService`)
- Repositories: `*Repository` suffix (e.g., `GameRepository`, `DeckRepository`)
- DTOs: `*Request` / `*Response` suffix (e.g., `CreateGameRequest`, `GameResponse`)
- Exceptions: `*Exception` suffix (e.g., `GameNotFoundException`)

**Variables and Methods:**
- Use descriptive, verbose names (e.g., `remainingCardsInDeck`, not `cards`)
- Methods: camelCase, verb-based (e.g., `dealCardsToPlayer`, `calculatePlayerScore`)
- Boolean methods: use `is`, `has`, `can` prefix (e.g., `isDeckEmpty`, `hasAvailableCards`)

**Constants and Enums:**
- Enums: UPPER_CASE (e.g., `Suit.HEARTS`, `Rank.KING`)
- Constants: UPPER_SNAKE_CASE (e.g., `MAX_CARDS_PER_DECK = 52`, `REDIS_KEY_PREFIX`)

### 2. Testing Standards

**Test Naming:**
- Pattern: `methodName_scenario_expectedResult`
- Examples:
  - `dealCards_whenDeckEmpty_returnsEmptyList()`
  - `shuffle_withStandardDeck_randomizesCardOrder()`
  - `addPlayer_whenPlayerAlreadyExists_throwsConflictException()`

**Test Structure:**
- Use `@DisplayName` for readable descriptions
- Follow AAA pattern: Arrange, Act, Assert
- Use `@MockBean` for Spring context mocks
- Integration tests: use `@SpringBootTest` + Testcontainers for Redis

**Test Coverage:**
- Minimum 70% code coverage
- 100% coverage for: shuffle algorithm, score calculation, core business rules
- Test edge cases: empty decks, dealing more cards than available, etc.

### 3. Git Commit Conventions

**Format:** `tipo(escopo): descrição`

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `test`: Adding or updating tests
- `refactor`: Code refactoring without behavior change
- `docs`: Documentation changes
- `chore`: Build, dependencies, configuration

**Examples:**
```
feat(game): implement Fisher-Yates shuffle algorithm
fix(deck): prevent dealing cards when deck is empty
test(player): add unit tests for score calculation
refactor(redis): optimize card storage structure
docs(api): add OpenAPI documentation for endpoints
chore(deps): update Spring Boot to 3.2.0
```

**Rules:**
- Keep description under 72 characters
- Use imperative mood ("add" not "added")
- Don't end with period
- Include issue reference if applicable: `feat(game): add shuffle endpoint (#123)`

### 4. Validation and Exception Handling

**Custom Exceptions (create in `exception/` package):**
- `GameNotFoundException` - Game ID not found
- `PlayerNotFoundException` - Player ID not found in game
- `DeckNotFoundException` - Deck ID not found
- `InvalidDealOperationException` - Cannot deal cards (deck empty, invalid count, etc.)
- `DuplicatePlayerException` - Player already exists in game
- `EmptyDeckException` - Attempting operation on empty deck

**Validation Rules:**
- Use `@Valid` on all request DTOs in controllers
- Use Bean Validation annotations: `@NotNull`, `@NotBlank`, `@Positive`, `@Min`, `@Max`
- Validate business rules in service layer, throw custom exceptions
- Never return null - throw exception or return Optional/empty collection

**Response Status Codes:**
- `200 OK` - Successful GET/POST/DELETE
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid input, validation errors
- `404 Not Found` - Resource not found
- `409 Conflict` - Duplicate resource, business rule violation
- `500 Internal Server Error` - Unexpected errors (Redis failure, etc.)

**Exception Response Format:**
```json
{
  "timestamp": "2026-06-12T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Game with ID 'game-123' not found",
  "path": "/api/v1/games/game-123"
}
```

## Development Commands

### Building
```bash
# Build project
./gradlew clean build

# Run tests
./gradlew test

# Run pre-commit checks (spotless + tests)
./gradlew preCommit
```

### Running Locally
```bash
# Start Redis
docker-compose up -d

# Run application
./gradlew bootRun

# Access Swagger UI
# http://localhost:8080/api/v1/swagger-ui.html

# Access API docs
# http://localhost:8080/api/v1/api-docs

# Health check
# http://localhost:8080/api/v1/actuator/health
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests GameServiceTest

# Run specific test method
./gradlew test --tests GameServiceTest.createGame_whenValidInput_returnsGameId

# Run tests with coverage
./gradlew test jacocoTestReport
```

### Code Formatting
```bash
# Check formatting issues
./gradlew spotlessCheck

# Apply formatting fixes
./gradlew spotlessApply
```

## Implementation Guidance

### When implementing the shuffle algorithm:
```java
// Example Fisher-Yates implementation
public void shuffle(List<Card> cards) {
    Random random = new Random();
    for (int i = cards.size() - 1; i > 0; i--) {
        int j = random.nextInt(i + 1);
        Card temp = cards.get(i);
        cards.set(i, cards.get(j));
        cards.set(j, temp);
    }
}
```

### Logging Standards
- **TRACE**: Shuffle algorithm details
- **DEBUG**: Card dealing operations, Redis operations
- **INFO**: Game/player creation/deletion
- **WARN**: Invalid operation attempts (e.g., dealing with no cards)
- **ERROR**: Redis failures, unhandled exceptions

Use structured logging (JSON format) with fields: `timestamp`, `level`, `logger`, `message`, `gameId`, `playerId`, etc.

### Error Responses
- `400 Bad Request` - Invalid input data
- `404 Not Found` - Game, player, or deck not found
- `409 Conflict` - Duplicate player, business rule violation
- `500 Internal Server Error` - Unexpected errors

### Testing Strategy
- **Unit tests**: Test domain logic, shuffle algorithm, score calculation
- **Integration tests**: Test Redis persistence, REST endpoints with Testcontainers
- **Target coverage**: 70%+ code coverage

## Architectural Decisions

### Redis as Primary Storage
- Chosen for excellent read/write performance and native data structures
- Use RDB/AOF persistence for durability
- Consider TTL for automatic cleanup of old games

### REST over WebSocket
- Synchronous REST API meets current requirements
- Simpler to implement and test
- WebSocket can be added later for real-time features

### Card Copying Strategy
- Copy cards into game deck (don't maintain references to original deck)
- Ensures immutability of deck templates
- Provides isolation between games

### No Authentication in Phase 1
- Focus on functional requirements first
- Add Spring Security in future phase

## Reference Documentation

See `DECK_GAME_PLAN.md` for:
- Detailed functional requirements
- Complete operation flows
- Phase-by-phase implementation plan (15-19 days estimated)
- API usage examples
- Additional enhancement suggestions
