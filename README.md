# TradeMaster: Production-Grade Fintech Trading Bot

[![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-Caching-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Container-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

<img src="./fintech-core/src/main/resources/static/TradeMaster-Fintech-Bot.webp" width="800" length="400" alt="TradeMaster Fintech Bot">

TradeMaster is a high-performance, production-ready Spring Boot application that powers a Telegram-first trading simulation bot. It offers a seamless bridge between real-time market data and automated trading strategies.

---

## Try it Live
You can interact with the bot directly on Telegram:
**[@TradeMasterFintechBot](https://t.me/TradeMasterFintechBot)**

---

## Key Features

- **Production Hardening**: Built with structured logging (MDC), input validation, and secure webhook handling.
- **Multi-Asset Support**: Real-time price tracking for Crypto, Stocks, and Fiat currencies.
- **Persistent Alert System**: 
  - Price-based (Above/Below) and Percentage-based (+5%, -10%) alerts.
  - Alarms survive application restarts (PostgreSQL backed).
- **Automated Trading**: Set /autobuy or /autosell rules to execute trades at specific price targets automatically.
- **Portfolio Management**: Real-time balance tracking, PnL calculations, and transaction history.
- **Rich UI**: Interactive Telegram menus with structured HTML formatting.

---

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.2
- **Data Persistence**: PostgreSQL (JPA/Hibernate)
- **Caching**: Redis
- **APIs**: CoinGecko (Crypto), Alpha Vantage (Stocks), Frankfurter (Fiat)
- **Deployment**: Docker & Docker Compose

---

## Quick Start

### 1. Prerequisites
- Docker & Docker Compose
- A Telegram Bot Token (from [@BotFather](https://t.me/botfather))

### 2. Environment Configuration
Clone the repository and create a `.env` file from the template:

```bash
cp .env.example .env
```

Edit the `.env` file with your API keys and Bot token.

### 3. Deploy with Docker
Launch the entire stack (App, PostgreSQL, Redis) with a single command:

```bash
docker-compose up -d --build
```

### 4. GitHub Codespaces Deployment
If you are running this in GitHub Codespaces:
1. Go to the **Ports** tab in your terminal area.
2. Find port `8080`, right-click on "Visibility", and change it to **Public**.
3. Copy the **Forwarded Address** (e.g., `https://abc-8080.preview.app.github.dev`).
4. Use this URL to set your Telegram Webhook:
   `https://<YOUR_CODESPACE_URL>/api/v1/telegram/webhook`

---

## Telegram Commands

| Command | Description |
| :--- | :--- |
| `/start` | Link your account and open the main menu |
| `/price <symbol>` | Get current price (e.g., `/price BTC`) |
| `/portfolio` | View your holdings and balance |
| `/buy <sym> <qty>` | Purchase an asset |
| `/sell <sym> <qty>` | Sell an asset |
| `/alert <sym> <target>`| Set price (e.g., `80000`) or % (e.g., `+5%`) alert |
| `/alerts` | List all active alerts |
| `/delalert <id>` | Delete an alert by ID |
| `/rules` | View all active trade rules |
| `/help` | Show all available commands |

---

## Architecture

The project follows a modular and clean architecture:
- **TelegramBotService**: Routing layer for incoming updates.
- **CommandDispatcher**: Extensible command processing logic.
- **MarketDataService**: Smart routing for multiple data providers.
- **AlertService**: Background scheduler for rule evaluation and notification.

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
