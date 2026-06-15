# 🚀 Quick Start Guide - Bruno Collection

## Executar um Jogo Completo em 5 Minutos

### Pré-requisitos
✅ Bruno instalado  
✅ API rodando em `http://localhost:8080`  
✅ Coleção aberta no Bruno  

---

## 🎯 Fluxo Rápido (15 requests)

### 1. Setup Game (3 requests)

#### 1.1 Create Game
```
Request: Game/Create Game
✨ Auto-salva: gameId
```

#### 1.2 Create Deck
```
Request: Deck/Create Deck
✨ Auto-salva: deckId
```

#### 1.3 Add Deck to Game
```
Request: Game Deck/Add Deck to Game
Usa: gameId, deckId
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
⚠️ Altere o body para "Bob"
```

#### 2.3 Add Player 3 (Charlie)
```
Request: Player/Add Player
Body: { "name": "Charlie" }
⚠️ Altere o body para "Charlie"
```

#### 2.4 Get Player IDs
```
Request: Player/Get Player IDs (Helper)
📋 Copie o ID do player que você quer usar
⚙️ Settings → Environment → Cole em playerId
```

---

### 3. Play Game (4 requests)

#### 3.1 Shuffle Deck
```
Request: Game Deck/Shuffle Game Deck
Embaralha o shoe do game
```

#### 3.2 Deal Cards (Alice)
```
Request: Player/Deal Cards
⚠️ Use o playerId de Alice (copiado no passo 2.4)
Params: count=5
```

#### 3.3 Deal Cards (Bob)
```
Request: Player/Deal Cards
⚠️ Altere playerId para Bob (pegue do passo 2.4)
Params: count=5
```

#### 3.4 Deal Cards (Charlie)
```
Request: Player/Deal Cards
⚠️ Altere playerId para Charlie (pegue do passo 2.4)
Params: count=5
```

---

### 4. View Results (4 requests)

#### 4.1 Player Scores
```
Request: Player/Get Player Scores
Vê ranking de todos os jogadores
```

#### 4.2 Game State
```
Request: Game/Get Game
Vê estado completo (players, cardsRemaining)
```

#### 4.3 Suit Counts
```
Request: Game Deck/Get Suit Counts
Conta cartas restantes por naipe
```

#### 4.4 Card Counts
```
Request: Game Deck/Get Card Counts
Conta cada carta específica restante
```

---

## 📊 Resultado Esperado

Após executar todas as requests, você verá:

✅ **3 jogadores** com 5 cartas cada  
✅ **37 cartas restantes** no shoe (52 - 15)  
✅ **Ranking** de jogadores por pontuação  
✅ **Contagem** de cartas por naipe e tipo  

---

## 🎮 Cenários Alternativos

### Cenário: Blackjack (6 decks)

```
1. Create Game → gameId
2. Create Deck (6x) → deck1, deck2, ..., deck6
3. Add Deck to Game (6x com cada deckId)
4. Shuffle Game Deck
5. Add 3 Players
6. Deal 2 cards each
7. Deal 1 more card (hit)
8. Get Player Scores
```

### Cenário: Texas Hold'em

```
1. Create Game → gameId
2. Create Deck → deckId
3. Add Deck to Game
4. Add 4 Players
5. Shuffle Game Deck
6. Deal 2 cards each (hole cards)
7. Get Player Scores
```

### Cenário: Draw All Cards

```
1. Create Game → gameId
2. Create Deck → deckId
3. Add Deck to Game
4. Add 1 Player
5. Shuffle Game Deck
6. Deal 52 cards (pega todas)
7. Get Player Cards (vê todas as 52)
8. Get Game (cardsRemaining = 0)
```

---

## 💡 Pro Tips

### Usar o mesmo Game para múltiplos testes
Não delete o game entre testes. Você pode:
- Adicionar mais decks ao shoe
- Adicionar/remover players
- Shufflar novamente
- Continuar dealing

### Resetar variáveis
Se quiser começar do zero:
```
1. Delete Game
2. Delete Deck
3. Vá em Settings → Environment
4. Limpe gameId, deckId, playerId
5. Comece novamente
```

### Múltiplos jogadores
Para testar com mais jogadores:
```
1. Execute Add Player 10 vezes
2. Altere o body cada vez: 
   - Player1, Player2, ..., Player10
3. Para deal, copie/cole a request
   e altere o playerId manualmente
```

### Ver todas as variáveis
```
Settings → Environment → Local
```
Você verá:
- baseUrl
- apiKey
- gameId (preenchido após Create Game)
- deckId (preenchido após Create Deck)
- playerId (preenchido após Add Player)

---

## 🐛 Problemas Comuns

### "playerId é do último jogador adicionado"
**Solução**: Copie o playerId correto do response de "Get Game"

```
1. Execute: Game/Get Game
2. Veja a lista de players na response
3. Copie o ID do player desejado
4. Cole manualmente na variável playerId
```

### "Não consigo deal cards"
**Verificar**:
- ✅ Deck foi adicionado ao game?
- ✅ Deck foi shufflado?
- ✅ playerId está correto?
- ✅ Há cartas disponíveis? (Get Game → cardsRemaining > 0)

### "Erro 409 ao deletar deck"
**Causa**: Deck está em uso por um game ativo  
**Solução**: Delete o game primeiro, depois o deck

### "Erro 404"
**Causa**: IDs (gameId, deckId, playerId) inválidos  
**Solução**: Execute as requests de criação novamente

---

## ⚡ Atalhos

```
Cmd/Ctrl + Enter    → Executar request
Cmd/Ctrl + E        → Trocar ambiente
Cmd/Ctrl + B        → Toggle sidebar
Cmd/Ctrl + /        → Buscar requests
```

---

## 🎬 Next Steps

Após dominar o fluxo básico, explore:

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

**Pronto para começar? Execute a primeira request: `Game/Create Game`** 🚀
