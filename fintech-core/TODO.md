# Todo

-[x] Add implementations to the Spring Initializer (Web, JPA, Postgres, Redis, RabbitMQ, Lombok, Docker Compose)

-[x] Database & Domain Modeling (The Foundation)

  -[x] Create User Entity (id, chatId, username, registeredAt)

  -[x] Create Asset or Watchlist Entity (Users ve semboller arasında ilişki kurmak için - ManyToMany)

  -[x] Create UserRepository and AssetRepository interfaces

  -[x] Configure application.yml for PostgreSQL Connection

-[ ] Service Layer (Business Logic & External APIs)

  -[ ] Implement PriceService interface (Generic design)

  -[ ] Implement CryptoService (CoinGecko Integration via RestTemplate)

  -[ ] Implement ForexService (Frankfurter API Integration for EUR/PLN etc.)

  -[ ] Implement StockService (Optional: Yahoo alternative or mock data for generic stocks)

  -[ ] Critical: Implement Redis Caching logic inside these services (Check Cache -> If Empty -> Fetch API -> Save Cache)

-[ ] Implement REST Backend Endpoints (Testable via Browser/Postman)

  -[ ] Create PriceController class (@RestController)

  -[ ] Implement GET /api/prices/btc (Returns JSON price of Bitcoin)

  -[ ] Implement GET /api/prices/eur-pln (Returns JSON price for Erasmus parity)

  -[ ] Implement GET /api/prices/{symbol} (Generic endpoint, e.g., /api/prices/ETH or /api/prices/USD-TRY)

  -[ ] Implement GET /api/status or /help (Returns system health check)

-[ ] User Management Logic (Backend)

  -[ ] Create UserService

  -[ ] Implement registerUser(Long chatId, String username) method

  -[ ] Implement addAssetToUser(Long chatId, String symbol) (Kullanıcının favorilerine eklemesi için)

-[ ] Async & Alerts (RabbitMQ)

  -[ ] Configure RabbitMQ Queues in RabbitConfig

  -[ ] Implement a Scheduler (Cron Job) that checks prices every 1 minute

  -[ ] Implement logic: If price changes > X%, send message to RabbitMQ Queue

-[ ] Telegram Integration (The Final Interface)

  -[ ] Implement TelegramBotService extending TelegramLongPollingBot

  -[ ] Connect Telegram Bot to PriceController or Services created above

  -[ ] Map Telegram commands (/start, /price btc) to backend methods