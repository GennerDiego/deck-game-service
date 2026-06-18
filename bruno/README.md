# Bruno API Collection - Deck Game Service

This Bruno collection contains all API endpoints organized by controller.

## 📁 Collection Structure

```
bruno/
├── environments/
│   ├── Local.bru          # Local environment (localhost)
│   └── Docker.bru         # Docker environment
├── Game/                  # Game Controller
│   ├── Create Game.bru
│   ├── Get Game.bru
│   └── Delete Game.bru
├── Deck/                  # Deck Controller (Templates)
│   ├── Create Deck.bru
│   ├── Get All Decks.bru
│   ├── Get Deck by ID.bru
│   └── Delete Deck.bru
├── Game Deck/             # Game Deck Operations (Shoe)
│   ├── Add Deck to Game.bru
│   ├── Shuffle Game Deck.bru
│   ├── Get Suit Counts.bru
│   └── Get Card Counts.bru
└── Player/                # Player Controller
    ├── Add Player.bru
    ├── Deal Cards.bru
    ├── Get Player Cards.bru
    ├── Get Player Scores.bru
    └── Remove Player.bru
```

## 🚀 How to Use

### 1. Install Bruno

```bash
# macOS
brew install bruno

# Or download from: https://www.usebruno.com/downloads
```

### 2. Open Collection

1. Open Bruno
2. Click "Open Collection"
3. Select the `/deck-game-service/bruno` folder

### 3. Select Environment

- Click the environment dropdown at the top
- Select **"Local"** (default) or **"Docker"**

### 4. Start the API

```bash
# Local
./gradlew bootRun

# Or via Docker
docker compose up
```

## 🔑 Environment Variables

The following variables are shared across all requests:

| Variable   | Description                                  | Initial Value                |
|------------|----------------------------------------------|------------------------------|
| `baseUrl`  | API base URL                                 | `http://localhost:8080/api/v1` |
| `apiKey`   | API Key for authentication                   | `default-api-key-change-me`  |
| `gameId`   | Current game ID (auto-populated)            | (empty)                      |
| `playerId` | Current player ID (auto-populated)          | (empty)                      |
| `deckId`   | Current deck ID (auto-populated)            | (empty)                      |

### Auto-populated Variables

The following requests **automatically save** IDs to the environment:

- ✅ **Create Game** → saves `gameId`
- ✅ **Create Deck** → saves `deckId`

### Get Player ID (manual)

After adding a player, you need to get the `playerId`:

1. Execute: **Player/Get Player IDs (Helper)**
2. In the response, find your player in the `players` array
3. Copy the `id` field of the desired player
4. Go to **Settings → Environment → Local**
5. Paste the ID in the `playerId` variable

Example:
```json
{
  "players": [
    { "id": "player-abc123", "name": "Alice" },
    { "id": "player-def456", "name": "Bob" }
  ]
}
```
To use Alice: `playerId = player-abc123`

## 📋 Recommended Test Flow

Execute requests in this order to test a complete flow:

### 1️⃣ Initial Setup
1. **Game/Create Game** → saves `gameId`
2. **Deck/Create Deck** → saves `deckId`
3. **Game Deck/Add Deck to Game** → adds deck to game

### 2️⃣ Add Players
4. **Player/Add Player** → saves `playerId`
5. Repeat to add more players (change name in body)

### 3️⃣ Play
6. **Game Deck/Shuffle Game Deck** → shuffles cards
7. **Player/Deal Cards** → deals cards (adjust `count` query param)
8. **Player/Get Player Cards** → view player's cards
9. **Player/Get Player Scores** → view player rankings

### 4️⃣ Queries
10. **Game/Get Game** → view complete game state
11. **Game Deck/Get Suit Counts** → count cards by suit
12. **Game Deck/Get Card Counts** → count each specific card

### 5️⃣ Cleanup
13. **Player/Remove Player** → remove player (optional)
14. **Game/Delete Game** → delete the game
15. **Deck/Delete Deck** → delete the deck (if not in use)

## 🔍 Request Features

### Automatic Headers
- Requests requiring authentication include `X-API-Key: {{apiKey}}` automatically
- GET requests **do not** require API key
- POST/DELETE requests **require** API key

### Included Tests
Each request has automatic tests that validate:
- ✅ Correct status code
- ✅ Response structure
- ✅ Expected values
- ✅ Business validations

View results in Bruno's "Tests" panel after executing each request.

### Post-Response Scripts
Some requests execute scripts after receiving the response:
- `Create Game` → saves gameId
- `Create Deck` → saves deckId
- `Add Player` → fetches game and saves playerId

### Documentation
Each request has a "Docs" tab with:
- Endpoint description
- Expected behavior
- Important notes

## 🎯 Tips

### Edit Variables Manually
If needed, you can edit variables directly:

1. Click the settings icon ⚙️
2. Select the environment (Local or Docker)
3. Edit variable values
4. Save

### Query Parameters
To change parameters (e.g., number of cards to deal):

1. Open the **Player/Deal Cards** request
2. Go to the "Params" tab
3. Change the `count` value

### Request Body
To change player name:

1. Open **Player/Add Player**
2. Go to the "Body" tab
3. Change the `name` value

### Run in Sequence
Use Bruno's "Run Collection" feature to execute all requests in order automatically!

## ⚡ Keyboard Shortcuts

- `Cmd/Ctrl + Enter` → Execute current request
- `Cmd/Ctrl + E` → Open environment selector
- `Cmd/Ctrl + /` → Search requests
- `Cmd/Ctrl + B` → Toggle sidebar

## 🐛 Troubleshooting

### Error 401 Unauthorized
- Check if the `apiKey` variable is configured correctly
- Ensure the `X-API-Key` header is present

### Error 404 Not Found
- Verify that `gameId`, `deckId`, `playerId` variables are populated
- Execute creation requests first

### Error 409 Conflict
- When deleting deck: deck is being used in an active game
- When adding player: a player with that name already exists

### Server not responding
```bash
# Check if server is running
curl http://localhost:8080/api/v1/actuator/health

# If not, start it:
./gradlew bootRun
# or
docker compose up
```

## 🔒 Concurrency Notes

All state-modifying operations (POST/DELETE) use distributed locking:
- **Automatic retry** on lock contention (up to 3 attempts with exponential backoff)
- **Thread-safe** across multiple instances
- **Auto-recovery** if lock holder crashes (10s TTL)

If you see a `ConcurrentOperationException`, another operation is in progress. The system will automatically retry.

## 📚 Additional Resources

- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
- **API Docs**: http://localhost:8080/api/v1/api-docs
- **Health Check**: http://localhost:8080/api/v1/actuator/health

## 🤝 Contributing

To add new endpoints to the collection:

1. Create a new `.bru` file in the appropriate folder
2. Use the format:
```
meta {
  name: Endpoint Name
  type: http
  seq: sequential_order
}

post/get/delete {
  url: {{baseUrl}}/path
  body: none|json
  auth: none
}

headers {
  X-API-Key: {{apiKey}}
}

tests {
  test("should return 200", function() {
    expect(res.status).to.equal(200);
  });
}
```

3. Add post-response scripts if needed to save variables
4. Document in the `docs` tab if necessary

---

🎮 **Happy Testing!**
