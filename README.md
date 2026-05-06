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
- Telegram `/start` now returns a REST access token for bearer-authenticated requests
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
- `POST /api/v1/auth/login` *(public bootstrap / compatibility; Telegram `/start` is the primary login path)*

### Market
- `GET /api/v1/market/price/{symbol}` *(requires `Authorization: Bearer <token>`)*
- Optional query: `assetType=CRYPTO|STOCK|FIAT`

### Portfolio
- `GET /api/v1/portfolio/me` *(requires `Authorization: Bearer <token>`)*
- `POST /api/v1/portfolio/watch` *(requires bearer token)*
- `POST /api/v1/portfolio/assets/{symbol}/buy` *(requires bearer token)*
- `POST /api/v1/portfolio/assets/{symbol}/sell` *(requires bearer token)*

### Telegram
- `POST /api/v1/telegram/webhook` *(public ingress; protected by Telegram secret header)*

### Security Model
- Telegram `/start` links the chat to a backend user and issues a bearer token.
- Telegram commands can use services directly without exposing public REST routes.
- Public endpoints are limited to auth bootstrap and Telegram webhook ingress; user data and trading routes require bearer auth.

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

### Prereq: Telegram Bot Oluşturma

1. Telegram'da `@BotFather` ile sohbet aç
2. `/newbot` yaz
3. Bot ismini gir (örn: `TradeMasterFintechBot`)
4. Bot handle'ı gir (örn: `TradeMasterFintechBot`)
5. Aldığın token'ı `.env` dosyasına ekle:
   ```env
   TELEGRAM_BOT_TOKEN=YOUR_BOT_TOKEN_HERE
   TELEGRAM_BOT_USERNAME=TradeMasterFintechBot
   TELEGRAM_WEBHOOK_SECRET=your_webhook_secret_here
   ```

### Step 1: Lokal Uygulamayı Başlat

```bash
cd fintech-core
.\mvnw.cmd spring-boot:run
```

App şu yükslenecek: `http://localhost:8080`

### Step 2: ngrok ile Public URL Oluştur

Yeni bir PowerShell terminal aç (çalışan app'i kapatma)

```bash
cd fintech-core\ngrok
.\ngrok.exe http 8080
```

**Çıktıdan public URL'yi kopyala:**
```
Forwarding    https://abc123.ngrok.io -> http://localhost:8080
```

### Step 3: Webhook'u Telegram API'ye Kaydet

Aşağıdaki komutu çalıştır (agora adımdan URL'yi kopyala):

```powershell
$botToken = "8555245412:AAEJway6-6WodhE4Lq_wqGd9shmKsfd4r7U"  # .env dosyasından kopyala
$publicUrl = "https://abc123.ngrok.io"                          # ngrok URL'sini kopyala
$secret = "a04e1708211c1b2b3c815fcbd199fae31381984b97c40c8c57b4638f0ec41f7b"  # .env WEBHOOK_SECRET

$uri = "https://api.telegram.org/bot$botToken/setWebhook?url=$publicUrl/api/v1/telegram/webhook&secret_token=$secret"

Invoke-RestMethod -Method Get -Uri $uri
```

**Başarılı sonuç:**
```json
{
    "ok": true,
    "result": true,
    "description": "Webhook was set"
}
```

### Step 4: Telegram Bot'u Test Et

Telegram'da `@TradeMasterFintechBot` (ya da bot handle'ını) ara ve:

```
/start
```

**Beklenen yanıt:**
```
Welcome back. Your account is linked securely with this Telegram chat.
REST access token: `<TOKEN>`
Keep this token private. Use it as: Authorization: Bearer <token>
```

### Webhook Debug / Sorun Giderme

**Webhook status'unu kontrol et:**
```powershell
$botToken = "YOUR_BOT_TOKEN"
Invoke-RestMethod -Method Get -Uri "https://api.telegram.org/bot$botToken/getWebhookInfo"
```

Çıktıdaki `"ok": true` ve `"pending_update_count": 0` olması gerekir.

**Eğer bot yanıt vermiyorsa:**
1. ✅ ngrok hâlâ çalışıyor mu? (terminal açık mı)
2. ✅ App hâlâ çalışıyor mu? (`spring-boot:run` terminal'inde log var mı)
3. ✅ `.env` içindeki `TELEGRAM_WEBHOOK_SECRET` ile `setWebhook` komutundaki `$secret` aynı mı?
4. ✅ Bot token geçerli mi? (test: `Invoke-RestMethod -Method Get -Uri "https://api.telegram.org/bot$botToken/getMe"`)
5. ✅ Uygulamadaki log'ları kontrol et:
   ```
   Handling Telegram update
   Chat ID: ..., Text: /start
   Processing /start command ...
   ```

### Step 5: REST API Test (Optional)

## REST Auth Flow

### Login ve Token Alma

Telegram bot'tan `/start` komutu gerçekleştir, ardından gelen **REST access token**'ı kopyala.

Alternatif olarak, REST API ile login yapabilirsin:

```powershell
$loginPayload = @{
    provider = "TELEGRAM"
    externalId = "YOUR_CHAT_ID"
    username = "YOUR_USERNAME"
} | ConvertTo-Json

$response = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/v1/auth/login" `
    -ContentType "application/json" `
    -Body $loginPayload

$token = $response.accessToken
Write-Host "Access Token: $token"
```

### Bearer Token ile Protected Endpoint'leri Kullan

```powershell
$token = "YOUR_ACCESS_TOKEN"
$headers = @{ Authorization = "Bearer $token" }

# Portfolio bilgisini al
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/api/v1/portfolio/me" `
    -Headers $headers | ConvertTo-Json

# Watchlist'e varlık ekle
$watchPayload = @{ symbol = "BTC" } | ConvertTo-Json
Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/v1/portfolio/watch" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $watchPayload
```

### Public Endpoint'ler (token gerekli değil)

```powershell
# Kripto fiyat sorgula
Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8080/api/v1/market/price/BTC?assetType=CRYPTO"

# Hisse fiyat sorgula (Alpha Vantage key gerekli)
Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8080/api/v1/market/price/AAPL?assetType=STOCK"
```

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
