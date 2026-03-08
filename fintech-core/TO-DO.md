# To-Do List

## Core Domain & Database (Entities)
- [ ] Create `User` entity.
    - Fields: `id` (UUID), `username`, `balance` (BigDecimal), `authProvider` (String), `externalId` (String).
- [ ] Create `Asset` entity.
    - Fields: `id` (UUID), `symbol` (String, unique), `name` (String).
- [ ] Create `UserAsset` entity.
    - Fields: `id` (UUID), `user` (ManyToOne), `asset` (ManyToOne), `quantity` (BigDecimal), `averageCost` (BigDecimal).

## Data Access (Repositories)
- [ ] Create `UserRepository`.
    - Add method: `Optional<User> findByAuthProviderAndExternalId(String provider, String externalId)`
- [ ] Create `AssetRepository`.
    - Add method: `Optional<Asset> findBySymbol(String symbol)`
- [ ] Create `UserAssetRepository`.
    - Add method: `List<UserAsset> findAllByUserId(UUID userId)`
    - Add method: `Optional<UserAsset> findByUserIdAndAsset_Symbol(UUID userId, String symbol)`

## Core Services (Business Logic)
- [ ] Create `AuthService` (Agnostic Authentication).
    - Add method: `registerOrLogin(String provider, String externalId, String username)`
    - Logic: Check if user exists with provider+externalId.
        - If yes: return user UUID.
        - If no: create new user with 100.000 USD balance and return UUID.
- [ ] Create `MarketDataService` (External API Integration).
    - Set up `RestTemplate` or `WebClient`.
    - Add method: `getCurrentPrice(String symbol)`
    - Test: Fetch a real price from a free API (e.g., Yahoo Finance or Binance API) and print it to the console.

## API Exposure (Controllers)
- [ ] Create `AuthController`.
    - Endpoint: `POST /api/v1/auth/login` (Accepts JSON with provider, externalId, username. Returns User ID).
- [ ] Create `MarketController`.
    - Endpoint: `GET /api/v1/market/price/{symbol}` (Returns the real-time price fetched from `MarketDataService`).