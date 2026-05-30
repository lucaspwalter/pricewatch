PriceWatch

O que é
PriceWatch é uma aplicação para acompanhar preços de produtos e avisar o usuário quando um item chega ao valor desejado.

A ideia é simples: o usuário cadastra um produto, define um preço-alvo e deixa o sistema monitorar a variação automaticamente. Quando o preço atual fica menor ou igual ao valor definido, o alerta é disparado e a notificação pode ser enviada por WhatsApp.

Como funciona
O backend consulta periodicamente os produtos cadastrados, registra o histórico de preços e verifica se algum alerta ativo atingiu o preço-alvo.

O usuário acessa o frontend, cria uma conta, cadastra produtos, define alertas e acompanha o status pelo dashboard. A API protege as rotas com autenticação JWT, persiste os dados em PostgreSQL e usa migrations Flyway para manter a estrutura do banco versionada.

Tecnologias
Backend:
- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- JWT
- Maven

Frontend:
- Next.js
- React
- TypeScript
- Tailwind CSS

Integrações:
- Fake Store API para consulta de produtos e preços
- Evolution API para envio de notificações por WhatsApp

Como rodar localmente
Pré-requisitos:
- Java 17
- Maven
- Node.js
- PostgreSQL

Crie um banco PostgreSQL para o projeto:

```sql
CREATE DATABASE pricewatch;
```

Configure as variáveis de ambiente do backend, se necessário:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/pricewatch
export DATABASE_USERNAME=pricewatch
export DATABASE_PASSWORD=pricewatch
export JWT_SECRET=sua-chave-secreta
```

Rode o backend:

```bash
mvn spring-boot:run
```

A API ficará disponível em:

```text
http://localhost:8080
```

Em outro terminal, rode o frontend:

```bash
cd frontend
npm install
npm run dev
```

O frontend ficará disponível em:

```text
http://localhost:3000
```

Estrutura do projeto
```text
pricewatch/
├── src/
│   └── main/
│       ├── java/com/pricewatch/
│       │   ├── client/        # Integrações externas
│       │   ├── config/        # Configurações da aplicação
│       │   ├── controller/    # Endpoints da API
│       │   ├── dto/           # Objetos de entrada e saída
│       │   ├── model/         # Entidades do domínio
│       │   ├── repository/    # Acesso ao banco de dados
│       │   ├── scheduler/     # Monitoramento periódico de preços
│       │   ├── security/      # Autenticação e autorização
│       │   └── service/       # Regras de negócio
│       └── resources/
│           ├── application.properties
│           └── db/migration/  # Migrations Flyway
├── frontend/
│   ├── app/                   # Telas da aplicação
│   ├── components/            # Componentes reutilizáveis
│   └── lib/                   # Cliente da API e utilitários
├── Dockerfile
└── pom.xml
```
