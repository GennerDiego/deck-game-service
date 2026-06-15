# Bruno API Collection - Deck Game Service

Esta coleção Bruno contém todos os endpoints da API organizados por controller.

## 📁 Estrutura da Coleção

```
bruno/
├── environments/
│   ├── Local.bru          # Ambiente local (localhost)
│   └── Docker.bru         # Ambiente Docker
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

## 🚀 Como Usar

### 1. Instalar Bruno

```bash
# macOS
brew install bruno

# Ou baixe de: https://www.usebruno.com/downloads
```

### 2. Abrir a Coleção

1. Abra o Bruno
2. Clique em "Open Collection"
3. Selecione a pasta `/deck-game-service/bruno`

### 3. Selecionar Ambiente

- Clique no dropdown de ambientes no topo
- Selecione **"Local"** (padrão) ou **"Docker"**

### 4. Iniciar a API

```bash
# Local
./gradlew bootRun

# Ou via Docker
docker compose up
```

## 🔑 Variáveis de Ambiente

As seguintes variáveis são compartilhadas entre todas as requests:

| Variável   | Descrição                                    | Valor Inicial                |
|------------|----------------------------------------------|------------------------------|
| `baseUrl`  | URL base da API                              | `http://localhost:8080/api/v1` |
| `apiKey`   | API Key para autenticação                    | `default-api-key-change-me`  |
| `gameId`   | ID do game atual (auto-preenchido)          | (vazio)                      |
| `playerId` | ID do player atual (auto-preenchido)        | (vazio)                      |
| `deckId`   | ID do deck atual (auto-preenchido)          | (vazio)                      |

### Variáveis Auto-preenchidas

As requests seguintes **automaticamente salvam** IDs no ambiente:

- ✅ **Create Game** → salva `gameId`
- ✅ **Create Deck** → salva `deckId`

### Obter Player ID (manual)

Após adicionar um player, você precisa obter o `playerId`:

1. Execute: **Player/Get Player IDs (Helper)**
2. Na resposta, encontre seu player na array `players`
3. Copie o campo `id` do player desejado
4. Vá em **Settings → Environment → Local**
5. Cole o ID na variável `playerId`

Exemplo:
```json
{
  "players": [
    { "id": "player-abc123", "name": "Alice" },
    { "id": "player-def456", "name": "Bob" }
  ]
}
```
Para usar Alice: `playerId = player-abc123`

## 📋 Fluxo de Teste Recomendado

Execute as requests nesta ordem para testar um fluxo completo:

### 1️⃣ Setup Inicial
1. **Game/Create Game** → salva `gameId`
2. **Deck/Create Deck** → salva `deckId`
3. **Game Deck/Add Deck to Game** → adiciona deck ao game

### 2️⃣ Adicionar Jogadores
4. **Player/Add Player** → salva `playerId`
5. Repita para adicionar mais jogadores (altere o nome no body)

### 3️⃣ Jogar
6. **Game Deck/Shuffle Game Deck** → embaralha as cartas
7. **Player/Deal Cards** → distribui cartas (ajuste `count` query param)
8. **Player/Get Player Cards** → vê as cartas do jogador
9. **Player/Get Player Scores** → vê ranking de jogadores

### 4️⃣ Consultas
10. **Game/Get Game** → vê estado completo do game
11. **Game Deck/Get Suit Counts** → conta cartas por naipe
12. **Game Deck/Get Card Counts** → conta cada carta específica

### 5️⃣ Cleanup
13. **Player/Remove Player** → remove jogador (opcional)
14. **Game/Delete Game** → deleta o game
15. **Deck/Delete Deck** → deleta o deck (se não estiver em uso)

## 🔍 Recursos das Requests

### Headers Automáticos
- Requests que requerem autenticação incluem `X-API-Key: {{apiKey}}` automaticamente
- GET requests **não** requerem API key
- POST/DELETE requests **requerem** API key

### Tests Incluídos
Cada request tem testes automáticos que validam:
- ✅ Status code correto
- ✅ Estrutura da resposta
- ✅ Valores esperados
- ✅ Validações de negócio

Veja os resultados no painel "Tests" do Bruno após executar cada request.

### Scripts Post-Response
Algumas requests executam scripts após receber a resposta:
- `Create Game` → salva gameId
- `Create Deck` → salva deckId
- `Add Player` → busca o game e salva playerId

### Documentação
Cada request tem uma aba "Docs" com:
- Descrição do endpoint
- Comportamento esperado
- Notas importantes

## 🎯 Dicas

### Editar Variáveis Manualmente
Se precisar, você pode editar as variáveis diretamente:

1. Clique no ícone de engrenagem ⚙️
2. Selecione o ambiente (Local ou Docker)
3. Edite os valores das variáveis
4. Salve

### Query Parameters
Para alterar parâmetros (ex: número de cartas para deal):

1. Abra a request **Player/Deal Cards**
2. Vá na aba "Params"
3. Altere o valor de `count`

### Request Body
Para mudar o nome do jogador:

1. Abra **Player/Add Player**
2. Vá na aba "Body"
3. Altere o valor de `name`

### Executar em Sequência
Use o recurso "Run Collection" do Bruno para executar todas as requests em ordem automaticamente!

## ⚡ Atalhos de Teclado

- `Cmd/Ctrl + Enter` → Executar request atual
- `Cmd/Ctrl + E` → Abrir seletor de ambiente
- `Cmd/Ctrl + /` → Buscar requests
- `Cmd/Ctrl + B` → Alternar sidebar

## 🐛 Troubleshooting

### Erro 401 Unauthorized
- Verifique se a variável `apiKey` está configurada corretamente
- Certifique-se que o header `X-API-Key` está presente

### Erro 404 Not Found
- Verifique se as variáveis `gameId`, `deckId`, `playerId` estão preenchidas
- Execute as requests de criação primeiro

### Erro 409 Conflict
- Ao deletar deck: o deck está sendo usado em um game ativo
- Ao adicionar player: já existe um player com esse nome

### Servidor não responde
```bash
# Verifique se o servidor está rodando
curl http://localhost:8080/api/v1/actuator/health

# Se não estiver, inicie:
./gradlew bootRun
# ou
docker compose up
```

## 📚 Recursos Adicionais

- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
- **API Docs**: http://localhost:8080/api/v1/api-docs
- **Health Check**: http://localhost:8080/api/v1/actuator/health

## 🤝 Contribuindo

Para adicionar novos endpoints à coleção:

1. Crie um novo arquivo `.bru` na pasta apropriada
2. Use o formato:
```
meta {
  name: Nome do Endpoint
  type: http
  seq: ordem_sequencial
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

3. Adicione scripts post-response se necessário salvar variáveis
4. Documente na aba `docs` se necessário

---

🎮 **Happy Testing!**
