# PriceWatch

PriceWatch is a full-stack price monitoring platform that tracks real product pages, records price history, and notifies users when a target price is reached.

<p align="center">
  <img src="docs/assets/stores/amazon.png" alt="Amazon" width="120" height="120" />
  <img src="docs/assets/stores/mercado-livre.png" alt="Mercado Livre" width="120" height="120" />
  <img src="docs/assets/stores/kabum.png" alt="KaBuM" width="120" height="120" />
  <img src="docs/assets/stores/aliexpress.png" alt="AliExpress" width="120" height="120" />
</p>

## Supported stores

| Store | Status | Integration |
| --- | --- | --- |
| Amazon Brazil | Supported | Product page extraction |
| Mercado Livre | Supported | Official Mercado Livre API only |
| KaBuM | Supported | Product page extraction |
| AliExpress | Supported | Browser-rendered product page extraction |

> Mercado Livre requires valid API credentials. HTML scraping is not used as a reliable fallback for blocked product pages.

## Features

- Real product page monitoring
- Configurable target-price alerts
- Automatic checks every 30 minutes
- Historical price records
- JWT authentication
- WhatsApp notification integration
- Domain allowlist and HTTPS URL validation
- Docker-based local environment

## Tech stack

- Java 17, Spring Boot, Spring Security, Spring Data JPA
- PostgreSQL and Flyway
- Next.js, React, TypeScript, and Tailwind CSS
- Selenium Chromium and Jsoup
- Docker Compose

## Getting started

Requirements: Docker and Docker Compose.

```bash
git clone https://github.com/lucaspwalter/pricewatch.git
cd pricewatch
cp .env.example .env
docker compose up --build
```

Open the web app at `http://localhost:3000`. The API runs at `http://localhost:8080`.

## Mercado Livre configuration

Create an application in the Mercado Livre developer portal and add its credentials to `.env`:

```env
MERCADOLIVRE_ACCESS_TOKEN=
MERCADOLIVRE_CLIENT_ID=
MERCADOLIVRE_CLIENT_SECRET=
```

Never commit `.env`, access tokens, client secrets, or notification API keys.

## Architecture

```text
pricewatch/
├── frontend/                  # Next.js web application
├── src/main/java/             # Spring Boot application
│   └── com/pricewatch/
│       ├── client/            # Store and notification integrations
│       ├── controller/        # REST endpoints
│       ├── model/             # Domain entities
│       ├── repository/        # Persistence layer
│       ├── scheduler/         # Automated price checks
│       ├── security/          # Authentication and authorization
│       └── service/           # Business logic
├── src/main/resources/        # Configuration and migrations
├── docs/assets/stores/        # Store logos
└── docker-compose.yml
```

## Security

- Secrets are loaded through environment variables.
- `.env` is excluded from Git.
- Only HTTPS URLs from approved store domains are accepted.
- Credentials and tokens must be rotated immediately if exposed.

## License

Licensed under the terms in [LICENSE](LICENSE).
