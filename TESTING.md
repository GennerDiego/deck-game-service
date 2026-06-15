# Testing Guide

## 🧪 Test Structure

```
src/test/java/com/cardgame/
├── AbstractIntegrationTest.java                      # Testcontainers setup
└── integration/                                      # Integration tests
    ├── BaseIntegrationTest.java                      # Shared helpers
    ├── GameManagementIntegrationTest.java            # 5 tests
    ├── DeckManagementIntegrationTest.java            # 7 tests
    ├── PlayerManagementIntegrationTest.java          # 5 tests
    ├── DealCardsIntegrationTest.java                 # 10 tests - CORE ⭐
    ├── QueryOperationsIntegrationTest.java           # 16 tests
    └── RealisticGameFlowIntegrationTest.java         # 6 tests - Complete Flows ⭐
```

**Total:** 56+ integration tests covering all endpoints and core requirements

---

## 🚀 Quick Start

### Run Integration Tests (Recommended)
```bash
./gradlew integrationTest
```

This custom task:
- ✅ Runs only integration tests (faster than all tests)
- ✅ Optimized JVM settings (1GB heap, G1GC)
- ✅ Parallel execution enabled
- ✅ Detailed output with emojis
- ✅ Automatically starts Redis via Testcontainers

---

## 📋 Common Commands

### All Tests
```bash
# Run all tests (unit + integration)
./gradlew test

# Run with coverage report
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

### Integration Tests Only
```bash
# Run all integration tests
./gradlew integrationTest

# With detailed logs
./gradlew integrationTest --info

# With stack traces on failure
./gradlew integrationTest --stacktrace
```

### Specific Test Files
```bash
# By class name
./gradlew test --tests "GameManagementIntegrationTest"
./gradlew test --tests "DealCardsIntegrationTest"
./gradlew test --tests "RealisticGameFlowIntegrationTest"

# Multiple classes
./gradlew test --tests "*ManagementIntegrationTest"
```

### Specific Test Methods
```bash
# Exact method
./gradlew test --tests "DealCardsIntegrationTest.dealCards_afterShuffle_dealsAll52CardsInRandomOrder"

# Pattern matching
./gradlew test --tests "DealCardsIntegrationTest.*shuffle*"
./gradlew test --tests "RealisticGameFlowIntegrationTest.pokerGameFlow*"
```

### By Package
```bash
# All integration tests
./gradlew test --tests "com.cardgame.integration.*"

# Specific subdomain
./gradlew test --tests "com.cardgame.integration.Realistic*"
```

---

## 🎯 Test Categories

### Core Requirements Tests (Must Pass)
```bash
# Deal 52 cards after shuffle + 53rd returns empty
./gradlew test --tests "DealCardsIntegrationTest.dealCards_afterShuffle*"
./gradlew test --tests "DealCardsIntegrationTest.dealCards_whenDeckIsEmpty*"

# Player scores sorted descending
./gradlew test --tests "QueryOperationsIntegrationTest.PlayerScoresTests.getPlayerScores_withMultiplePlayers*"

# Card counts sorted by suit and value
./gradlew test --tests "QueryOperationsIntegrationTest.CardCountsTests.getCardCounts_withFullDeck*"

# Face values correct (Ace=1, Jack=11, Queen=12, King=13)
./gradlew test --tests "QueryOperationsIntegrationTest.PlayerScoresTests.getPlayerScores_usesCorrectFaceValues"
```

### Realistic Game Flows
```bash
# Poker game (4 players, 5 cards each)
./gradlew test --tests "RealisticGameFlowIntegrationTest.pokerGameFlow*"

# Blackjack (6-deck shoe, multiple rounds)
./gradlew test --tests "RealisticGameFlowIntegrationTest.blackjackGameFlow*"

# Complete lifecycle (player leaves, cleanup)
./gradlew test --tests "RealisticGameFlowIntegrationTest.completeGameLifecycle*"

# Deck exhaustion
./gradlew test --tests "RealisticGameFlowIntegrationTest.drawAllCardsScenario*"

# Progressive shuffle at different stages
./gradlew test --tests "RealisticGameFlowIntegrationTest.progressiveShuffle*"

# All query operations mid-game
./gradlew test --tests "RealisticGameFlowIntegrationTest.queryOperationsMidGame*"
```

---

## 🐛 Troubleshooting

### Docker Not Running
```
Error: Could not start container
Solution: Start Docker Desktop
```

### Port Already in Use
```
Error: Port 8080 already in use
Solution: Tests use random ports (@LocalServerPort), ensure no conflicts
```

### Redis Container Fails
```bash
# Check Docker
docker ps

# Clean up old containers
docker system prune -f

# Restart Docker Desktop
```

### Out of Memory
```bash
# Increase heap size in build.gradle.kts
maxHeapSize = "2048m"  # Instead of 1024m
```

### Tests Hang
```bash
# Kill Gradle daemon
./gradlew --stop

# Try again
./gradlew integrationTest
```

---

## 📊 Coverage Report

```bash
# Generate coverage report
./gradlew test jacocoTestReport

# Open HTML report
open build/reports/jacoco/test/html/index.html

# Target: 70%+ code coverage
# Focus: 100% for shuffle algorithm, score calculation, core business rules
```

---

## 🔧 Task Configuration

The `integrationTest` task is defined in `build.gradle.kts`:

```kotlin
tasks.register("integrationTest", Test::class) {
    description = "Run integration tests only"
    group = "verification"

    // Include only integration package
    filter {
        includeTestsMatching("com.cardgame.integration.*")
    }

    // Performance tuning
    maxHeapSize = "1024m"
    jvmArgs("-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100")
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1

    // Detailed output
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
```

---

## 📝 Writing New Tests

### 1. Extend BaseIntegrationTest
```java
@DisplayName("My Feature Tests")
class MyFeatureIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Should do something when condition")
    void myTest_whenCondition_doesSomething() {
        // Given
        Game game = createGame();
        addDeckToGame(game.getId());
        
        // When
        // ... perform action
        
        // Then
        // ... verify result
    }
}
```

### 2. Use Helper Methods
```java
// Available helpers from BaseIntegrationTest:
createGame()
getGame(gameId)
deleteGame(gameId)
addDeckToGame(gameId)
shuffleDeck(gameId)
addPlayer(gameId, playerName)
removePlayer(gameId, playerId)
dealCards(gameId, playerId, count)
getPlayerCards(gameId, playerId)
getPlayerScores(gameId)
getSuitCounts(gameId)
getCardCounts(gameId)
```

### 3. Follow Naming Convention
```
methodName_whenCondition_expectedResult
```

Examples:
- `createGame_whenCalled_returnsGameWithId`
- `dealCards_whenDeckEmpty_returnsEmptyList`
- `shuffleDeck_afterDealing_onlyShufflesRemainingCards`

---

## 🎓 Best Practices

1. **Use descriptive test names** with `@DisplayName`
2. **Follow AAA pattern** (Arrange, Act, Assert)
3. **Test one thing per test** - don't combine unrelated assertions
4. **Use helper methods** from BaseIntegrationTest (DRY)
5. **Verify full flow** - not just happy paths
6. **Test edge cases** - empty decks, boundary conditions, etc.
7. **Keep tests independent** - no shared state between tests
8. **Clean up resources** - Testcontainers handles this automatically

---

## 📚 Additional Documentation

- **[CLAUDE.md](CLAUDE.md)** - Project overview and development guidelines
- **[build.gradle.kts](build.gradle.kts)** - Build configuration and tasks
