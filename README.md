# TradeMaster: FinTech Trading Bot Core

![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Caching-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Container-2496ED?style=for-the-badge&logo=docker&logoColor=white)

## Project Overview
TradeMaster is a high-performance, event-driven backend simulation designed for real-time stock trading. The core objective of the project is to demonstrate critical FinTech concepts, including low-latency data processing, transactional integrity (ACID), and asynchronous messaging systems.

The system architecture focuses on fetching real-time market data, ensuring high availability through caching, and managing user portfolios with strict data consistency.

## Architecture and Tech Stack
The project is built on a modular Spring Boot 3 architecture, emphasizing scalability and maintainability:

* **Backend Framework:** Java 21, Spring Boot (Web, Data JPA).
* **Persistence Layer:** PostgreSQL for managing user wallets, historical transactions, and portfolio data.
* **Performance Layer:** Redis serves as a high-speed caching mechanism to deliver market data with sub-10ms latency.
* **Messaging:** RabbitMQ facilitates an event-driven notification system, decoupling price alerts from the main trading engine.
* **Infrastructure:** Docker and Docker Compose for standardized environment orchestration.

## Technical Implementation Details
* **Market Data Synchronization:** Integrated with external finance APIs (CoinGecko & Frankfurt) to provide real-time updates supported by a Redis caching strategy.
* **Transactional Trading Engine:** Implementation of `@Transactional` boundaries to ensure atomic operations, preventing race conditions such as double spending or negative balance updates.
* **Asynchronous Alert System:** An event-driven architecture using RabbitMQ to process price targets and trigger notifications without impacting core system performance.
* **Scalable Data Modeling:** Relational database schema optimized for portfolio tracking and real-time profit/loss calculations.

## Project Structure
The development follows a phased engineering approach:
1. **Infrastructure:** Standardized Docker configurations for PostgreSQL, Redis, and RabbitMQ.
2. **Data Integration:** Service layer implementation for external API consumption and cache-aside pattern with Redis.
3. **Core Logic:** Development of robust Buy/Sell services with enforced business rules and data integrity checks.
4. **Event Processing:** Integration of message brokers for scalable user notification workflows.

## Getting Started

### Prerequisites
* Java 21 or higher
* Docker Desktop

### Installation and Execution
1. Clone the repository:
   ```bash
   git clone [https://github.com/muhammetcnli/TradeMaster-Fintech-Bot](https://github.com/muhammetcnli/TradeMaster-Fintech-Bot)
