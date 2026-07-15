# PriceWatch

## O que é

PriceWatch é um sistema para acompanhar o preço de produtos automaticamente.

Ele funciona como um "avise-me quando baixar". A pessoa cadastra um produto, escolhe o preço que quer pagar e o sistema fica verificando se aquele produto chegou no valor desejado.

Quando o preço atual fica menor ou igual ao preço escolhido, o sistema desativa o alerta e mostra a mudança no painel.

## Como funciona

O projeto tem duas partes:

- Backend: é a API, a parte que salva os usuários, produtos, alertas, histórico de preços e notificações.
- Frontend: é a tela que a pessoa usa no navegador para criar conta, entrar no sistema e cadastrar alertas.

O backend verifica os preços a cada 30 minutos. Ele busca o preço atual do produto, grava esse valor no histórico e compara com o preço-alvo cadastrado pelo usuário.

## Tecnologias

Backend:

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL

## Como rodar localmente

Com Docker instalado:

```bash
git clone https://github.com/lucaspwalter/pricewatch.git
cd pricewatch
docker compose up --build
```

Acesse `http://localhost:3000`. API: `http://localhost:8080`.
- Flyway
- JWT
- Maven

Frontend:

- Next.js
- React
- TypeScript
- Tailwind CSS

Integração:

- Fake Store API para buscar produtos e preços

## Como usar

A explicação de como usar o PriceWatch está no meu portfólio:

https://lucaspwalter.github.io/portfolio/setup-pricewatch.html

## Estrutura do projeto

```text
pricewatch/
├── src/
│   └── main/
│       ├── java/com/pricewatch/
│       │   ├── client/        # Conexões com serviços externos
│       │   ├── config/        # Configurações do sistema
│       │   ├── controller/    # Rotas da API
│       │   ├── dto/           # Dados que entram e saem da API
│       │   ├── model/         # Tabelas e regras principais
│       │   ├── repository/    # Comunicação com o banco
│       │   ├── scheduler/     # Verificação automática de preços
│       │   ├── security/      # Login, senha e token JWT
│       │   └── service/       # Regras de negócio
│       └── resources/
│           ├── application.properties
│           └── db/migration/  # Criação e alteração das tabelas
├── frontend/
│   ├── app/                   # Páginas do site
│   ├── components/            # Partes reutilizáveis da tela
│   └── lib/                   # Comunicação com a API
├── Dockerfile
└── pom.xml
```
