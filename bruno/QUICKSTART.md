# 🚀 Quick Start Guide - Bruno Collection

## Run a Complete Game in 5 Minutes

### Prerequisites
✅ Bruno installed  
✅ API running at `http://localhost:8080`  
✅ Collection open in Bruno  

---

## 🎯 Quick Flow (15 requests)

### 1. Setup Game (3 requests)

#### 1.1 Create Game
```
Request: Game/Create Game
✨ Auto-saves: gameId
```

#### 1.2 Create Deck
```
Request: Deck/Create Deck
✨ Auto-saves: deckId
```

#### 1.3 Add Deck to Game
```
Request: Game Deck/Add Deck to Game
Uses: gameId, deckId
```

---

### 2. Add Players (4 requests)

#### 2.1 Add Player 1 (Alice)
```
Request: Player/Add Player
Body: { "name": "Alice" }
```

#### 2.2 Add Player 2 (Bob)
```
Request: Player/Add Player
Body: { "name": "Bob" }
⚠️ Change body to "Bob"
```

#### 2.3 Add Player 3 (Charlie)
```
Request: Player/Add Player
Body: { "name": "Charlie" }
⚠️ Change body to "Charlie"
```

#### 2.4 Get Player IDs
```
Request: Player/Get Player IDs (Helper)
📋 Copy the ID of the player you want to use
⚙️ Settings → Environment → Paste in playerId
```

---

### 3. Play Game (4 requests)

#### 3.1 Shuffle Deck
```
Request: Game Deck/Shuffle Game Deck
Shuffles the game shoe
```

#### 3.2 Deal Cards (Alice)
```
Request: Player/Deal Cards
⚠️ Use Alice's playerId (copied in step 2.4)
Params: count=5
```

#### 3.3 Deal Cards (Bob)
```
Request: Player/Deal Cards
⚠️ Change playerId to Bob (get from step 2.4)
Params: count=5
```

#### 3.4 Deal Cards (Charlie)
```
Request: Player/Deal Cards
⚠️ Change playerId to Charlie (get from step 2.4)
Params: count=5
```

---

### 4. View Results (4 requests)

#### 4.1 Player Scores
```
Request: Player/Get Player Scores
View ranking of all players
```

#### 4.2 Game State
```
Request: Game/Get Game
View complete state (players, cardsRemaining)
```

#### 4.3 Suit Counts
```
Request: Game Deck/Get Suit Counts
Count remaining cards by suit
```

#### 4.4 Card Counts
```
Request: Game Deck/Get Card Counts
Count each specific remaining card
```

---

## 📊 Expected Result

After executing all requests, you'll see:

✅ **3 players** with 5 cards each  
✅ **37 cards remaining** in the shoe (52 - 15)  
✅ **Ranking** of players by score  
✅ **Counts** of cards by suit and type  

---

## 🎮 Alternative Scenarios

### Scenario: Blackjack (6 decks)

```
1. Create Game → gameId
2. Create Deck (6x) → deck1, deck2, ..., deck6
3. Add Deck to Game (6x with each deckId)
4. Shuffle Game Deck
5. Add 3 Players
6. Deal 2 cards each
7. Deal 1 more card (hit)
8. Get Player Scores
```

### Scenario: Texas Hold'em

```
1. Create Game → gameId
2. Create Deck → deckId
3. Add Deck to Game
4. Add 4 Players
5. Shuffle Game Deck
6. Deal 2 cards each (hole cards)
7. Get Player Scores
```

### Scenario: Draw All Cards

```
1. Create Game → gameId
2. Create Deck → deckId
3. Add Deck to Game
4. Add 1 Player
5. Shuffle Game Deck
6. Deal 52 cards (takes all)
7. Get Player Cards (see all 52)
8. Get Game (cardsRemaining = 0)
```

---

## 💡 Pro Tips

### Use the same Game for multiple tests
Don't delete the game between tests. You can:
- Add more decks to the shoe
- Add/remove players
- Shuffle again
- Continue dealing

### Reset variables
If you want to start from scratch:
```
1. Delete Game
2. Delete Deck
3. Go to Settings → Environment
4. Clear gameId, deckId, playerId
5. Start again
```

### Multiple players
To test with more players:
```
1. Execute Add Player 10 times
2. Change the body each time: 
   - Player1, Player2, ..., Player10
3. To deal, copy/paste the request
   and change playerId manually
```

### View all variables
```
Settings → Environment → Local
```
You'll see:
- baseUrl
- apiKey
- gameId (populated after Create Game)
- deckId (populated after Create Deck)
- playerId (populated after Add Player)

---

## 🐛 Common Issues

### "playerId is from the last added player"
**Solution**: Copy the correct playerId from "Get Game" response

```
1. Execute: Game/Get Game
2. See the players list in the response
3. Copy the ID of the desired player
4. Paste manually in the playerId variable
```

### "Can't deal cards"
**Check**:
- ✅ Was deck added to game?
- ✅ Was deck shuffled?
- ✅ Is playerId correct?
- ✅ Are there cards available? (Get Game → cardsRemaining > 0)

### "Error 409 when deleting deck"
**Cause**: Deck is in use by an active game  
**Solution**: Delete the game first, then the deck

### "Error 404"
**Cause**: Invalid IDs (gameId, deckId, playerId)  
**Solution**: Execute creation requests again

### "ConcurrentOperationException"
**Cause**: Another operation is in progress on the same game  
**Solution**: Wait and try again - automatic retry will handle it (3 attempts with 50ms → 100ms → 200ms backoff)

---

## ⚡ Shortcuts

```
Cmd/Ctrl + Enter    → Execute request
Cmd/Ctrl + E        → Switch environment
Cmd/Ctrl + B        → Toggle sidebar
Cmd/Ctrl + /        → Search requests
```

---

## 🎬 Next Steps

After mastering the basic flow, explore:

1. **Deck Operations**
   - Get All Decks
   - Get Deck by ID
   - Delete unused decks

2. **Player Operations**
   - Get Player Cards (individual)
   - Remove Player
   - Add players with different names

3. **Advanced Queries**
   - Suit Counts (card counting)
   - Card Counts (specific cards)
   - Player Scores (leaderboard)

4. **Swagger UI**
   - Compare with Bruno collection
   - Use "Try it out" feature
   - See OpenAPI documentation

---

**Ready to start? Execute the first request: `Game/Create Game`** 🚀
