# To-Do List: TradeMaster Fintech Bot

## Core Domain & Database (Entities)
- [x] Create `User` entity.
- [x] Create `Asset` entity.
- [x] Create `UserAsset` entity.
- [ ] Create `Transaction` entity.
  - Fields: `id`, `user`, `asset`, `type` (BUY/SELL/WATCH), `quantity`, `price`, `timestamp`.

## Data Access (Repositories)
- [x] Create `UserRepository`.
- [x] Create `AssetRepository`.
- [x] Create `UserAssetRepository`.
- [ ] Create `TransactionRepository`.
  - Method: `List<Transaction> findAllByUserId(UUID userId)`.

## Core Services (Business Logic)
- [x] Create `AuthService`.
- [x] Create `MarketDataService`.
- [ ] Create `PortfolioService` (The Core Engine).
  - **`getPortfolio(UUID userId)`**: Kullanıcının sahip olduğu tüm `UserAsset` listesini ve toplam USD değerini döner.
  - **`buyAsset(UUID userId, String symbol, BigDecimal quantity)`**:
    1. Güncel fiyatı `MarketDataService`'den al.
    2. `balance` kontrolü yap.
    3. `UserAsset` güncelle (yoksa oluştur, varsa miktar ekle ve ortalama maliyet `averageCost` hesapla).
    4. `Transaction` kaydı at.
  - **`watchAsset(UUID userId, String symbol)`**: Kullanıcıya 0 miktarlı bir `UserAsset` oluştur veya sadece izleme listesi tablosuna ekle.

## API Exposure (Controllers & Routing)
- [x] Create `AuthController`.
  - `POST /api/v1/auth/login`: (Telegram'dan gelen `externalId` ile kullanıcı yoksa otomatik oluşturur, varsa giriş yapar. Register ve Login burada birleşir).
- [ ] Create `PortfolioController`.
  - `GET /api/v1/portfolio`: Kullanıcının varlıklarını listeler.
  - `POST /api/v1/portfolio/assets/{symbol}/buy`: Belirli bir varlığı satın alır.
  - `POST /api/v1/portfolio/assets/{symbol}/watch`: Varlığı takibe alır.
- [x] Create `MarketController`.

## Telegram & Integration
- [ ] Create `TelegramBotService`.
  - Telegram'dan gelen `/start` mesajında `AuthService.registerOrLogin` metodunu tetikle.
  - `/price BTC` veya `/buy BTC 0.5` komutlarını işle.