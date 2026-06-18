# Deck Game Service

A Spring Boot REST API service that implements a poker-style deck of cards game system with Redis as the primary data store.

## 🚀 Quick Start

### Prerequisites
- **Docker & Docker Compose** (recommended - easiest setup)
- **Java 17+** and **Gradle 8.8** (required for building)

### Option 1: Run with Docker (Recommended ⭐)

```bash
# Clone the repository
git clone <repository-url>
cd deck-game-service

# Configure API key (optional - defaults to 'default-api-key-change-me')
echo "API_KEY=my-secret-key" > .env

# Build JAR locally
./gradlew clean bootJar

# Start everything with Docker (app + Redis)
docker compose up -d --build
```

> **⚡ TL;DR - Two commands to start:**
> ```bash
> ./gradlew clean bootJar
> docker compose up -d --build
> # Default API Key: default-api-key-change-me
> # Access: http://localhost:8080/api/v1/swagger-ui.html
> ```

**Access the application:**
- Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
- API Docs: http://localhost:8080/api/v1/api-docs
- Health Check: http://localhost:8080/api/v1/actuator/health

**Stop services:**
```bash
docker compose down

# Remove volumes (reset Redis data)
docker compose down -v
```

### Option 2: Run Locally (Development)

1. **Start Redis**
   ```bash
   docker compose up -d redis
   ```

2. **Configure API key (optional)**
   ```bash
   export API_KEY=my-local-dev-key
   # Or use default: 'default-api-key-change-me'
   ```

3. **Build the project**
   ```bash
   ./gradlew clean build
   ```

4. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

5. **Access the API**
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

### Docker Commands

```bash
# Build JAR and start all services
./gradlew clean bootJar
docker compose up -d --build

# View logs
docker compose logs -f
docker compose logs -f app  # Only app logs

# Rebuild after code changes
./gradlew clean bootJar
docker compose up -d --build

# Stop services
docker compose down

# Stop and remove volumes (reset Redis)
docker compose down -v

# Check service status
docker compose ps

# Execute commands inside container
docker compose exec app bash
```

### Gradle Commands

```bash
# Build
./gradlew clean build

# Build without tests
./gradlew clean build -x test

# Run tests
./gradlew test

# Run unit tests only
./gradlew unitTest

# Run integration tests only
./gradlew integrationTest

# Run specific test
./gradlew test --tests GameServiceTest

# Apply code formatting (Spotless)
./gradlew spotlessApply

# Check code formatting
./gradlew spotlessCheck

# Pre-commit checks (formatting + tests)
./gradlew preCommit

# Run with coverage report
./gradlew test jacocoTestReport
```

## ✨ Features

### Game Management
- Create and delete games
- Add multiple decks to a game (shoe)
- Shuffle game deck using manual Fisher-Yates algorithm
- **Thread-safe operations** with distributed locking

### Player Management
- Add players to a game (with duplicate detection)
- Remove players from a game
- Track player scores based on card face values
- **Concurrent-safe** player operations

### Card Operations
- Deal cards to players atomically
- View player's cards
- List all players with scores (sorted by highest to lowest)
- **Lock-protected** card distribution

### Game Deck Queries
- Count undealt cards by suit
- Count each remaining card in deck (sorted by suit and value)

### Concurrency & Reliability
- **Distributed locking**: Redis-based locks prevent race conditions
- **Automatic retry**: Exponential backoff on lock contention (50ms → 100ms → 200ms)
- **Auto-recovery**: Locks expire after 10s if app crashes (no orphan locks)
- **Multi-instance safe**: Works across multiple app instances

### Business Rules
- Standard deck: 52 cards (4 suits × 13 ranks)
- Once a deck is added to a game, it **cannot be removed**
- Shuffle algorithm must be implemented manually (no library shuffle)
- Cards can be dealt until deck is empty
- Score calculation: Ace=1, 2-10=face value, Jack=11, Queen=12, King=13
- All state-modifying operations are atomic and thread-safe

## 🔐 Authentication

The API uses **API Key authentication** for state-changing operations (POST/DELETE endpoints).

### 🔑 Default API Key

```
default-api-key-change-me
```

**How to use:**
```bash
# Include in all POST/DELETE requests:
curl -X POST http://localhost:8080/api/v1/games \
  -H "X-API-Key: default-api-key-change-me"
```

### Configure Custom API Key

**For Docker Compose:**
```bash
# Create .env file
echo "API_KEY=your-secret-key" > .env

# Start services
docker compose up
```

**For Local Development:**
```bash
# Export environment variable
export API_KEY=your-secret-key

# Run application
./gradlew bootRun
```

### Protected Endpoints (Require X-API-Key header)

All **POST** and **DELETE** operations require authentication:

```bash
# Include API key in all POST/DELETE requests
curl -X POST http://localhost:8080/api/v1/games \
  -H "X-API-Key: default-api-key-change-me"
```

### Public Endpoints (No authentication required)

All **GET** operations are public:
- `GET /api/v1/games/{gameId}`
- `GET /api/v1/games/{gameId}/players/{playerId}/cards`
- `GET /api/v1/games/{gameId}/players/scores`
- `GET /api/v1/games/{gameId}/deck/suits-count`
- `GET /api/v1/games/{gameId}/deck/cards-count`

## 📡 API Endpoints

### Game Management

```bash
# Create a game (requires API key)
POST /api/v1/games
Header: X-API-Key: your-api-key

# Get game details (public)
GET /api/v1/games/{gameId}

# Delete a game (requires API key)
DELETE /api/v1/games/{gameId}
Header: X-API-Key: your-api-key
```

### Deck Management

```bash
# Create a new deck (requires API key)
POST /api/v1/decks
Header: X-API-Key: your-api-key
Response: { "id": "deck-456", "cards": [...], "createdAt": "..." }

# Get all decks (public)
GET /api/v1/decks

# Get deck by ID (public)
GET /api/v1/decks/{deckId}

# Delete a deck - only if not in use by any game (requires API key)
DELETE /api/v1/decks/{deckId}
Header: X-API-Key: your-api-key

# Add existing deck to game shoe (requires API key)
POST /api/v1/games/{gameId}/deck/{deckId}
Header: X-API-Key: your-api-key

# Shuffle game deck/shoe (requires API key)
POST /api/v1/games/{gameId}/deck/shuffle
Header: X-API-Key: your-api-key

# Count undealt cards by suit (public)
GET /api/v1/games/{gameId}/deck/suits-count

# Count each remaining card (public)
GET /api/v1/games/{gameId}/deck/cards-count
```

### Player Management

```bash
# Add player to game (requires API key)
POST /api/v1/games/{gameId}/players
Header: X-API-Key: your-api-key
Body: { "name": "Alice" }

# Remove player from game (requires API key)
DELETE /api/v1/games/{gameId}/players/{playerId}
Header: X-API-Key: your-api-key

# Get all players with scores - sorted (public)
GET /api/v1/games/{gameId}/players/scores
```

### Card Operations

```bash
# Deal cards to player (requires API key)
POST /api/v1/games/{gameId}/players/{playerId}/deal?count=5
Header: X-API-Key: your-api-key

# Get player's cards (public)
GET /api/v1/games/{gameId}/players/{playerId}/cards
```

## 🎮 Usage Example

```bash
# Set API key for convenience
API_KEY="default-api-key-change-me"

# 1. Create a game
curl -X POST http://localhost:8080/api/v1/games \
  -H "X-API-Key: $API_KEY"
# Response: { "id": "game-123", ... }

# 2. Create a deck
curl -X POST http://localhost:8080/api/v1/decks \
  -H "X-API-Key: $API_KEY"
# Response: { "id": "deck-456", "cards": [...], "createdAt": "..." }

# 3. Add deck to game's shoe
curl -X POST http://localhost:8080/api/v1/games/game-123/deck/deck-456 \
  -H "X-API-Key: $API_KEY"

# 4. Add a player
curl -X POST http://localhost:8080/api/v1/games/game-123/players \
  -H "X-API-Key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice"}'

# 5. Get game to retrieve player ID
curl -X GET http://localhost:8080/api/v1/games/game-123
# Response includes players array with IDs

# 6. Shuffle the deck/shoe
curl -X POST http://localhost:8080/api/v1/games/game-123/deck/shuffle \
  -H "X-API-Key: $API_KEY"

# 7. Deal 5 cards to Alice
curl -X POST "http://localhost:8080/api/v1/games/game-123/players/player-789/deal?count=5" \
  -H "X-API-Key: $API_KEY"

# 8. View Alice's cards (public endpoint - no API key)
curl -X GET http://localhost:8080/api/v1/games/game-123/players/player-789/cards

# 9. View player rankings (public endpoint - no API key)
curl -X GET http://localhost:8080/api/v1/games/game-123/players/scores

# 10. Delete deck (only works if not in use by any game)
curl -X DELETE http://localhost:8080/api/v1/decks/deck-456 \
  -H "X-API-Key: $API_KEY"
# Returns 409 Conflict if deck is in use by a game
```

## 📋 Implementation Status

### Completed ✅
- ✅ Project structure and package organization
- ✅ Gradle configuration with Kotlin DSL
- ✅ Domain models (Card, Deck, Game, Player, Suit, Rank)
- ✅ Docker Compose with Redis
- ✅ Application configuration (Spring Boot, Redis, Swagger)
- ✅ Spotless code formatting with Google Java Format
- ✅ Logging configuration (console + file with profiles)
- ✅ Redis repository layer (generic JSON repository pattern)
- ✅ Service layer (GameService, DeckService, PlayerService)
- ✅ REST controllers (GameController, DeckController, PlayerController)
- ✅ Custom exceptions and global exception handler
- ✅ Shuffle utility (Fisher-Yates algorithm)
- ✅ Input validation (Bean Validation + business rules)
- ✅ Unit tests (128 tests - 94% coverage)
- ✅ Integration tests with Testcontainers (46 tests)
- ✅ API Key authentication (X-API-Key header for POST/DELETE)
- ✅ API documentation with OpenAPI/Swagger
- ✅ CI/CD pipeline (GitHub Actions)
- ✅ Docker multi-stage build
- ✅ Duplicate player name validation (case-insensitive)

## 🧪 Testing

The project includes comprehensive test coverage:

### Test Statistics
- **Total Tests**: 174 tests
  - Unit Tests: 128 tests (controllers, services, utilities, exception handlers)
  - Integration Tests: 46 tests (full API + Redis with Testcontainers)
- **Code Coverage**: 94.08%
- **Test Framework**: JUnit 5, Mockito, AssertJ, Testcontainers

### Test Commands

```bash
# Run all tests (unit + integration)
./gradlew test

# Run unit tests only (no Redis container)
./gradlew unitTest

# Run integration tests only (with Testcontainers)
./gradlew integrationTest

# Run specific test class
./gradlew test --tests GameControllerTest

# Run with coverage report
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

### Test Categories

**Unit Tests:**
- Controllers: API contract validation with mocked services
- Services: Business logic with mocked repositories
- Utilities: Fisher-Yates shuffle algorithm
- Exception Handlers: Error response formatting
- Interceptors: API key authentication logic

**Integration Tests:**
- Full API workflows with real Redis (Testcontainers)
- Game management, deck operations, player management
- Card dealing and scoring
- Duplicate name validation
- API key authentication enforcement

## 🐳 Docker Setup

### Architecture

The application uses a **multi-stage Docker build** for optimization:

```
Stage 1: Builder
├── gradle:8.8-jdk17-alpine (Build environment)
├── Compiles source code
└── Creates JAR artifact

Stage 2: Runtime
├── eclipse-temurin:17-jre-alpine (Minimal JRE)
├── Copies JAR from builder
├── Non-root user (spring:spring)
└── Final image: ~150MB
```

### Docker Compose Services

| Service | Image | Port | Description |
|---------|-------|------|-------------|
| `app` | deck-game-service:latest | 8080 | Spring Boot application |
| `redis` | redis:7.2-alpine | 6379 | Data store |

### Environment Variables

Configure via `.env` file (copy from `.env.example`):

```bash
# API Key for authentication
API_KEY=your-secure-api-key-here

# JVM Options
JAVA_OPTS=-Xmx512m -Xms256m
```

### Health Checks

Both services include health checks:
- **Application**: `curl http://localhost:8080/api/v1/actuator/health`
- **Redis**: `redis-cli ping`

### Volumes

- `redis-data`: Persists Redis data between restarts

## 🔧 Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.6 |
| Build Tool | Gradle 8.8 (Kotlin DSL) |
| Database | Redis 7.2 |
| Concurrency | Redis-based distributed locks with retry |
| API Documentation | OpenAPI 3.0 (Swagger) |
| Testing | JUnit 5, Mockito, Testcontainers (192 tests) |
| Code Coverage | 94% (Jacoco) |
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

## 🚀 CI/CD Pipeline

The project includes a **GitHub Actions** pipeline that runs on every push and pull request.

### Pipeline Stages

#### 1. **Unit Tests & Code Coverage** ⚡
- Runs Spotless code formatting check
- Executes 128 unit tests (controllers, services, utilities)
- Generates Jacoco coverage report (target: 70%+, current: 94%)
- Posts coverage summary as PR comment
- **Duration**: ~30 seconds

#### 2. **Integration Tests** 🔗
- Starts Redis using Docker services
- Runs 46 integration tests with Testcontainers
- Tests full API workflows with real Redis
- Validates authentication, game flows, and data persistence
- **Duration**: ~45 seconds

#### 3. **Smoke Test - Docker** 🔥
- Builds Docker image with multi-stage build
- Starts services with Docker Compose (app + Redis)
- Waits for application health endpoint
- Verifies application is running correctly
- **Duration**: ~1-2 minutes

### Pipeline Triggers

```yaml
# Runs on:
- Every push to any branch
- Every pull request
- Manual workflow dispatch
```

### Pipeline Status

Check pipeline status in:
- **GitHub Actions tab**: https://github.com/your-org/deck-game-service/actions
- **PR checks**: Appears as "CI" check on pull requests
- **Branch protection**: Configure to require CI passing before merge

### Coverage Reporting

The pipeline automatically posts code coverage to PRs:

```
📊 Code Coverage Report
━━━━━━━━━━━━━━━━━━━━━━━
✅ Good - Coverage: 94.08%

Coverage Levels:
✅ Excellent: ≥80%
✅ Good: ≥70%
🟡 Acceptable: ≥60%
🔴 Needs Improvement: <60%
```

### Local CI Simulation

Run the same checks locally before pushing:

```bash
# 1. Code formatting
./gradlew spotlessCheck

# 2. Unit tests
./gradlew unitTest

# 3. Coverage report
./gradlew jacocoTestReport

# 4. Integration tests
./gradlew integrationTest

# 5. Docker smoke test
docker compose up --build -d
curl http://localhost:8080/api/v1/actuator/health
docker compose down
```

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