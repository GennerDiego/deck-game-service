# Deck Game Service

A Spring Boot REST API service that implements a poker-style deck of cards game system with Redis as the primary data store.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Gradle 8.8 (included via wrapper)

### Running the Application

1. **Start Redis**
   ```bash
   docker-compose up -d
   ```

2. **Build the project**
   ```bash
   ./gradlew clean build
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

4. **Access the API**
   - Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
   - API Docs: http://localhost:8080/api/v1/api-docs
   - Health Check: http://localhost:8080/api/v1/actuator/health

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/cardgame/
│   │   ├── controller/       # REST API endpoints
│   │   ├── service/          # Business logic
│   │   ├── repository/       # Redis data access
│   │   ├── model/
│   │   │   ├── entity/       # Domain models (Card, Deck, Game, Player)
│   │   │   └── dto/          # Request/Response DTOs
│   │   ├── config/           # Spring configuration
│   │   ├── exception/        # Custom exceptions
│   │   ├── util/             # Utilities (shuffle algorithm, etc.)
│   │   └── validator/        # Validation logic
│   └── resources/
│       ├── application.yml   # Application configuration
│       └── logback-spring.xml # Logging configuration
└── test/
    └── java/com/cardgame/    # Test files
```

## 🎯 Domain Model

- **Card**: Represents a single card with `Suit` and `Rank`
- **Deck**: Standard 52-card deck
- **Game**: Game instance with game deck (shoe), players, and dealt cards
- **Player**: Game participant

### Enums
- **Suit**: HEARTS, SPADES, CLUBS, DIAMONDS
- **Rank**: ACE (1), TWO (2), ..., KING (13)

## 🛠️ Development Commands

```bash
# Build
./gradlew clean build

# Run tests
./gradlew test

# Run specific test
./gradlew test --tests GameServiceTest

# Apply code formatting (Spotless)
./gradlew spotlessApply

# Check code formatting
./gradlew spotlessCheck

# Pre-commit checks (formatting + tests)
./gradlew preCommit
```

## ✨ Features

### Game Management
- Create and delete games
- Add multiple decks to a game (shoe)
- Shuffle game deck using manual Fisher-Yates algorithm

### Player Management
- Add players to a game
- Remove players from a game
- Track player scores based on card face values

### Card Operations
- Deal cards to players
- View player's cards
- List all players with scores (sorted by highest to lowest)

### Game Deck Queries
- Count undealt cards by suit
- Count each remaining card in deck (sorted by suit and value)

### Business Rules
- Standard deck: 52 cards (4 suits × 13 ranks)
- Once a deck is added to a game, it **cannot be removed**
- Shuffle algorithm must be implemented manually (no library shuffle)
- Cards can be dealt until deck is empty
- Score calculation: Ace=1, 2-10=face value, Jack=11, Queen=12, King=13

## 📡 API Endpoints

### Game Management
```bash
# Create a game
POST /api/v1/games

# Delete a game
DELETE /api/v1/games/{gameId}

# Shuffle game deck
POST /api/v1/games/{gameId}/deck/shuffle
```

### Deck Management
```bash
# Create a standard 52-card deck
POST /api/v1/decks

# Add deck to game
POST /api/v1/games/{gameId}/decks
Body: { "deckId": "deck-uuid" }
```

### Player Management
```bash
# Add player to game
POST /api/v1/games/{gameId}/players
Body: { "name": "Alice" }

# Remove player from game
DELETE /api/v1/games/{gameId}/players/{playerId}
```

### Card Operations
```bash
# Deal cards to player
POST /api/v1/games/{gameId}/players/{playerId}/deal?count=5

# Get player's cards
GET /api/v1/games/{gameId}/players/{playerId}/cards

# Get all players with scores (sorted)
GET /api/v1/games/{gameId}/players/scores
```

### Game Deck Queries
```bash
# Count undealt cards by suit
GET /api/v1/games/{gameId}/deck/suits-count

# Count each remaining card
GET /api/v1/games/{gameId}/deck/cards-count
```

## 🎮 Usage Example

```bash
# 1. Create a game
curl -X POST http://localhost:8080/api/v1/games
# Response: { "gameId": "game-123" }

# 2. Create a deck
curl -X POST http://localhost:8080/api/v1/decks
# Response: { "deckId": "deck-456" }

# 3. Add deck to game
curl -X POST http://localhost:8080/api/v1/games/game-123/decks \
  -H "Content-Type: application/json" \
  -d '{"deckId": "deck-456"}'

# 4. Add a player
curl -X POST http://localhost:8080/api/v1/games/game-123/players \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice"}'
# Response: { "playerId": "player-789" }

# 5. Shuffle the deck
curl -X POST http://localhost:8080/api/v1/games/game-123/deck/shuffle

# 6. Deal 5 cards to Alice
curl -X POST "http://localhost:8080/api/v1/games/game-123/players/player-789/deal?count=5"

# 7. View Alice's cards
curl -X GET http://localhost:8080/api/v1/games/game-123/players/player-789/cards

# 8. View player rankings
curl -X GET http://localhost:8080/api/v1/games/game-123/players/scores
```

## 📋 Implementation Status

### Completed ✅
- Project structure and package organization
- Gradle configuration with Kotlin DSL
- Domain models (Card, Deck, Game, Player, Suit, Rank)
- Docker Compose with Redis
- Application configuration (Spring Boot, Redis, Swagger)
- Spotless code formatting with Google Java Format
- Logging configuration

### In Progress ⏳
- Redis repository layer
- Service layer (GameService, DeckService, PlayerService)
- REST controllers
- Custom exceptions and global exception handler
- Shuffle utility (Fisher-Yates algorithm)
- Input validation
- Unit tests
- Integration tests with Testcontainers

### Planned 📝
- API documentation with OpenAPI/Swagger annotations
- Metrics and monitoring (Micrometer)
- Performance testing
- CI/CD pipeline

## 🧪 Testing

The project includes:
- **Unit Tests**: Domain logic, shuffle algorithm, score calculation
- **Integration Tests**: Redis persistence, REST endpoints with Testcontainers
- **Test Coverage Target**: 70%+ code coverage

```bash
# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

## 🔧 Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.6 |
| Build Tool | Gradle 8.8 (Kotlin DSL) |
| Database | Redis 7.2 |
| API Documentation | OpenAPI 3.0 (Swagger) |
| Testing | JUnit 5, Mockito, Testcontainers |
| Code Formatting | Spotless + Google Java Format |
| Containerization | Docker + Docker Compose |

## 🤝 Contributing

### Development Workflow

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd deck-game-service
   ```

2. **Start Redis**
   ```bash
   docker-compose up -d
   ```

3. **Build the project**
   ```bash
   ./gradlew clean build
   ```

4. **Before committing**
   ```bash
   # Format code
   ./gradlew spotlessApply
   
   # Run tests
   ./gradlew test
   
   # Or run both
   ./gradlew preCommit
   ```

### Commit Convention

Follow the commit message format:
```
type(scope): description

Examples:
feat(game): implement Fisher-Yates shuffle algorithm
fix(deck): prevent dealing cards when deck is empty
test(player): add unit tests for score calculation
refactor(redis): optimize card storage structure
docs(api): add OpenAPI documentation for endpoints
```

**Types**: `feat`, `fix`, `test`, `refactor`, `docs`, `chore`

### Coding Standards

- Follow naming conventions defined in [CLAUDE.md](CLAUDE.md)
- Write descriptive variable names
- Test naming: `methodName_scenario_expectedResult()`
- Minimum 70% code coverage
- Always run Spotless before committing

## 🐛 Troubleshooting

### Redis Connection Error
```
Error: Unable to connect to Redis at localhost:6379
```
**Solution**: Make sure Redis is running
```bash
docker-compose up -d
docker-compose ps  # Check Redis is running
```

### Build Fails with Java Version Error
```
Error: Gradle requires Java 17 or higher
```
**Solution**: Install Java 17+
```bash
# Using SDKMAN
sdk install java 17.0.11-tem
sdk use java 17.0.11-tem
```

### Spotless Check Fails
```
Error: The following files had format violations
```
**Solution**: Apply Spotless formatting
```bash
./gradlew spotlessApply
```

### Port 8080 Already in Use
```
Error: Port 8080 is already in use
```
**Solution**: Stop other applications or change port in `application.yml`
```yaml
server:
  port: 8081
```

## 📖 Documentation

- **[CLAUDE.md](CLAUDE.md)** - Development guidelines for Claude Code (coding standards, architecture decisions)
- **[DECK_GAME_PLAN.md](DECK_GAME_PLAN.md)** - Complete implementation plan with phases and timelines
- **Swagger UI** - Interactive API documentation at http://localhost:8080/api/v1/swagger-ui.html

## 📄 License

This project is a technical assessment implementation. See implementation details in [DECK_GAME_PLAN.md](DECK_GAME_PLAN.md).