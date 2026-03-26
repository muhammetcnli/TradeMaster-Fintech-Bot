# TradeMaster: FinTech Trading Bot Core

![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Caching-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Container-2496ED?style=for-the-badge&logo=docker&logoColor=white)

TradeMaster is a Spring Boot backend for a Telegram-first trading simulation bot.
It supports user login/linking, real-time market price queries, watchlist tracking, buy/sell operations, and rule-based alert/auto-trade flows.

## Current MVP Status

Implemented in the current codebase:

- User identity flow via `POST /api/v1/auth/login` and Telegram `/start`
- Price fetching with provider routing by asset type (`CRYPTO`, `STOCK`, `FIAT`)
- Portfolio engine: buy/sell, average cost, PnL, balance updates, transaction logging
- Watchlist support (`/watch`, `/watchlist`) on Telegram and REST
- Telegram webhook endpoint with secret token header validation
- Scheduled evaluation for in-memory alerts and one-shot auto-trade rules

## Tech Stack

- Java 21
- Spring Boot (`webmvc`, `data-jpa`, `data-redis`, `amqp`)
- PostgreSQL
- Redis
- RabbitMQ (configured in properties for upcoming async flows)
- External APIs: CoinGecko, Alpha Vantage, Frankfurter

## Architecture Notes

- `PortfolioService` handles core transactional trading logic with `@Transactional`
- `MarketDataService` dispatches requests to provider-specific clients by `AssetType`
- Telegram commands are processed in `TelegramBotService`
- Webhook security is enforced in `TelegramWebhookController` using `X-Telegram-Bot-Api-Secret-Token`

## API Endpoints

### Auth
- `POST /api/v1/auth/login`

### Market
- `GET /api/v1/market/price/{symbol}`
- Optional query: `assetType=CRYPTO|STOCK|FIAT`

### Portfolio
- `GET /api/v1/portfolio/{id}`
- `POST /api/v1/portfolio/watch/`
- `POST /api/v1/portfolio/assets/{symbol}/buy`
- `POST /api/v1/portfolio/assets/{symbol}/sell`

### Telegram
- `POST /api/v1/telegram/webhook`

## Telegram Commands

- `/start`
- `/price BTC`
- `/watch BTC`
- `/watchlist`
- `/portfolio`
- `/buy BTC 0.1`
- `/sell BTC 0.1`
- `/alert BTC 80000 UP`
- `/autobuy BTC 70000 0.01`
- `/autosell BTC 90000 0.01`
- `/rules`

## Configuration

Application loads optional `.env` values with:

`spring.config.import=optional:file:.env[.properties]`

Create `.env` in project root:

```env
TELEGRAM_BOT_USERNAME=your_bot_username
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_WEBHOOK_SECRET=your_webhook_secret

COINGECKO_API_KEY=
ALPHA_VANTAGE_API_KEY=your_alpha_vantage_key
```

Default infra values from `src/main/resources/application.properties`:

- PostgreSQL: `localhost:5432/fintechdb` (`user` / `password`)
- Redis: `localhost:6379`
- RabbitMQ: `localhost:5672` (`user` / `password`)

## Local Run

### 1) Start infra services

If you do not have a compose file in this repo yet, run containers manually:

```powershell
docker run -d --name tm-postgres -e POSTGRES_DB=fintechdb -e POSTGRES_USER=user -e POSTGRES_PASSWORD=password -p 5432:5432 postgres:16
docker run -d --name tm-redis -p 6379:6379 redis:7
docker run -d --name tm-rabbitmq -e RABBITMQ_DEFAULT_USER=user -e RABBITMQ_DEFAULT_PASS=password -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 2) Run the app

```powershell
.\mvnw.cmd spring-boot:run
```

App base URL: `http://localhost:8080`

## Telegram Webhook Setup

Expose local app (for example using ngrok):

```powershell
.\ngrok\ngrok.exe http 8080
```

Set webhook with secret token:

```powershell
$botToken = "<TELEGRAM_BOT_TOKEN>"
$publicUrl = "https://<your-ngrok-domain>"
$secret = "<TELEGRAM_WEBHOOK_SECRET>"
$uri = "https://api.telegram.org/bot$botToken/setWebhook?url=$publicUrl/api/v1/telegram/webhook&secret_token=$secret"
Invoke-RestMethod -Method Post -Uri $uri
```

Quick local verification:

- Wrong secret header should return `403 Forbidden`
- Correct secret header should return `200 OK`

## Testing

Run tests:

```powershell
.\mvnw.cmd test
```

Current tests include:

- `TelegramWebhookControllerTest` (secret validation behavior)
- `FintechCoreApplicationTests` (context boot)

## Roadmap (Near-Term)

- Persist alerts/auto-trade rules in DB (instead of in-memory maps)
- Add richer Telegram notifications for watchlist triggers
- Improve integration and service-level test coverage
- Add production-ready deployment manifests (compose/k8s)

## Repository

- GitHub: `https://github.com/muhammetcnli/TradeMaster-Fintech-Bot`
