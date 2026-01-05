# TradeMaster: FinTech Trading Bot Core 🚀

![Java](https://img.shields.io/badge/Java-21%2B-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![Redis](https://img.shields.io/badge/Redis-Caching-red)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Event%20Driven-orange)

## 📖 Overview
**TradeMaster** is a high-performance, event-driven backend simulation of a real-time stock trading platform. Designed with a **Microservices mindset**, this project aims to demonstrate core FinTech concepts such as **low-latency data processing**, **transactional integrity (ACID)**, and **asynchronous messaging**.

The system fetches real-time market data, caches it for high availability, manages user portfolios with strict transactional safety, and delivers asynchronous price alerts via a Telegram Bot interface.

## 🏗 Architecture & Tech Stack
The project is built using **Spring Boot 3** and follows a modular architecture:

* **Core Backend:** Java 17, Spring Boot (Web, Data JPA).
* **Database:** PostgreSQL (Persisting user wallets, transactions, and portfolio data).
* **Caching layer:** Redis (Handling high-frequency market data to reduce DB load).
* **Message Broker:** RabbitMQ (Decoupling the notification system from the trading engine).
* **Containerization:** Docker & Docker Compose.

## ⚡ Key Features (Planned)
- **Real-time Market Data:** Fetches and caches stock prices (Yahoo Finance API wrapper).
- **Trading Engine:** Buy/Sell orders with transactional balance checks.
- **Portfolio Management:** Real-time P/L (Profit/Loss) calculation.
- **Price Alerts:** Users set targets; system fires async notifications via Telegram.

---

## 🗺️ Development Roadmap & To-Do

### Phase 1: Infrastructure & Configuration (Current Focus) 🛠️
- [x] Initialize Spring Boot Project with Dependencies (Web, JPA, Redis, RabbitMQ, Postgres).
- [x] Create `docker-compose.yml` to spin up PostgreSQL, Redis, and RabbitMQ locally.
- [ ] Configure `application.properties` to connect Spring Boot to Docker services.
- [ ] Verify connectivity (Application starts without errors).

### Phase 2: Market Data Service 📈
- [ ] Integrate an external Finance API (Yahoo Finance or Alpha Vantage).
- [ ] Implement **Redis Caching**: Cache stock prices to serve `<10ms` responses.
- [ ] Create a Scheduler to update prices periodically (e.g., every 10 seconds).

### Phase 3: Core Trading Logic 💰
- [ ] Design Database Schema (`Users`, `Portfolio`, `Transactions`).
- [ ] Implement `BuyService` and `SellService` with **@Transactional**.
- [ ] Ensure atomic updates (prevent negative balance or double spending).

### Phase 4: Notification System 🔔
- [ ] Set up **RabbitMQ** exchanges and queues (`price.alert.queue`).
- [ ] Create a Producer that sends an event when a price target is hit.
- [ ] Create a Consumer (Telegram Bot) that listens to the queue and messages the user.

---

## 🚀 Getting Started (How to Run)

### Prerequisites
- Java 17+
- Docker & Docker Compose

### Installation
1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/trademaster-fintech-bot.git](https://github.com/yourusername/trademaster-fintech-bot.git)