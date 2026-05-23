# Marketsense: AI-Driven Stock Market Analytics Platform

**Marketsense** is a full-stack stock market analytics dashboard designed to bridge the gap between raw market data and actionable insights. By combining real-time stock data from Finnhub with generative AI analysis via Google Gemini, the platform provides users with a comprehensive view of market trends, sentiment analysis, and structural indicators.

## Core Features

* **Real-Time Market Monitoring:** Integrated TradingView components for real-time scrolling ticker tapes, advanced technical charting, and market-wide sector heatmaps.
* **AI-Powered Discovery & Sentiment:** Utilizes Google Gemini AI to parse financial news headlines, extracting underlying sentiment scores and structural outlooks for specific stocks.
* **Performance Optimized Caching:** Implements Caffeine-based caching configurations in Spring Boot with multi-tier TTL policies to shield API limits and optimize system latency.
* **State-Managed Watchlists:** Custom personalized stock watchlist system mapped to individual users for tracking targeted financial portfolios.
* **Dockerized Infrastructure:** Containerized database setup using Docker Compose to ensure predictable environment reproducibility across localized setups.

---

## Technical Architecture & Stack

The application relies on a decoupled, production-grade client-server topology.

* **Frontend:** React 18, Tailwind CSS, React Router, Lucide Icons.
* **Backend:** Java 17+, Spring Boot 3, Spring Security (JWT), Spring Data JPA.
* **Database:** PostgreSQL (Containerized).
* **Caching Store:** Caffeine Cache (In-Memory JVM Cache).
* **Data Integrations:** Google Gemini API, Finnhub Financial API, TradingView Data Layer.

---

## Getting Started

### Prerequisites

Ensure you have the following installed on your machine:

* **Docker & Docker Compose**
* **Java Development Kit (JDK) 17+**
* **Node.js 18+ & npm**

### Installation & Orchestration Steps

#### 1. Environment Configuration

Navigate to the `Backend` directory and initialize your environment variables:

```bash
cd Backend
cp .env.example .env
# Edit the .env file with your actual API keys and database credentials

```

#### 2. Database Provisioning (Docker)

Spin up the localized PostgreSQL instance using Docker Compose from the root directory:

```bash
docker-compose up -d

```

#### 3. Backend Configuration & Execution

The application uses the following configuration in `Backend/src/main/resources/application.properties`:

```properties
# Database Connectivity Configuration
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.config.import=optional:file:.env[.properties]

# Security Settings (Token Expiry Configured to 7 Days)
security.jwt.expiration-time=604800000

# Third-Party External Core API Connectors
finnhub.api-key=${FINNHUB_API_KEY}
gemini.api-key=${GEMINI_API_KEY}

```

Execute the Spring Boot backend instance:

```bash
./mvnw spring-boot:run

```

#### 4. Frontend App Instantiation

Navigate to the frontend application workspace to install dependencies and initialize the development server:

```bash
cd Frontend
npm install
npm run dev

```

---

## System Performance & Optimization Strategy

To bypass high-frequency external network overhead and API call rate limitations, Marketsense enforces explicit internal caching strategies:

* **Stocks Engine Cache:** Evaluates with a **1-minute** write expiration window to preserve micro-level data accuracy.
* **Market Insights Cache:** Evaluates with a **30-minute** window for expensive text analysis and news summaries, minimizing structural runtime overhead.

---

*Disclaimer: This system is developed exclusively as an engineering project. Financial analytics and summaries served via the platform do not constitute official financial advisory statements.*