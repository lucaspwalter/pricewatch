# PriceWatch

## What it is

PriceWatch is a system that automatically tracks product prices.

It works like a "notify me when the price drops" service. Users add a product, choose their target price, and the system checks whether the product has reached that price.

When the current price is less than or equal to the target price, the system disables the alert and displays the change on the dashboard.

## How it works

The project has two parts:

- Backend: the API that stores users, products, alerts, price history, and notifications.
- Frontend: the browser interface used to create an account, sign in, and manage alerts.

The backend checks prices every 30 minutes. It retrieves each product's current price, saves it to the history, and compares it with the user's target price.

## Technologies

Backend:

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL

## Running locally

With Docker installed:

```bash
git clone https://github.com/lucaspwalter/pricewatch.git
cd pricewatch
docker compose up --build
```

Open `http://localhost:3000`. API: `http://localhost:8080`.
- Flyway
- JWT
- Maven

Frontend:

- Next.js
- React
- TypeScript
- Tailwind CSS

Integration:

- Fake Store API for product and price data

## How to use

Instructions for using PriceWatch are available in my portfolio:

https://lucaspwalter.github.io/portfolio/setup-pricewatch.html

## Project structure

```text
pricewatch/
├── src/
│   └── main/
│       ├── java/com/pricewatch/
│       │   ├── client/        # Connections to external services
│       │   ├── config/        # System configuration
│       │   ├── controller/    # API routes
│       │   ├── dto/           # API input and output data
│       │   ├── model/         # Database tables and core rules
│       │   ├── repository/    # Database access
│       │   ├── scheduler/     # Automatic price checks
│       │   ├── security/      # Login, passwords, and JWTs
│       │   └── service/       # Business rules
│       └── resources/
│           ├── application.properties
│           └── db/migration/  # Database table creation and changes
├── frontend/
│   ├── app/                   # Website pages
│   ├── components/            # Reusable UI components
│   └── lib/                   # API communication
├── Dockerfile
└── pom.xml
```
