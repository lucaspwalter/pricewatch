PriceWatch

O que é
PriceWatch é um sistema para acompanhar o preço de produtos automaticamente.

Ele funciona como um "avise-me quando baixar". A pessoa cadastra um produto, escolhe o preço que quer pagar e o sistema fica verificando se aquele produto chegou no valor desejado.

Quando o preço atual fica menor ou igual ao preço escolhido, o sistema desativa o alerta e mostra a mudança no painel.

Como funciona
O projeto tem duas partes:

- Backend: é a API, a parte que salva os usuários, produtos, alertas, histórico de preços e notificações.
- Frontend: é a tela que a pessoa usa no navegador para criar conta, entrar no sistema e cadastrar alertas.

O backend verifica os preços a cada 30 minutos. Ele busca o preço atual do produto, grava esse valor no histórico e compara com o preço-alvo cadastrado pelo usuário.

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

Integração:
- Fake Store API para buscar produtos e preços

Como rodar localmente
Os comandos abaixo foram escritos para Linux/Ubuntu.

1. Baixe o projeto do GitHub:

```bash
git clone https://github.com/lucaspwalter/pricewatch.git
cd pricewatch
```

2. Instale os programas necessários:

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk maven nodejs npm postgresql postgresql-contrib
```

3. Confira se tudo foi instalado:

```bash
java -version
mvn -version
node -v
npm -v
psql --version
```

4. Inicie o PostgreSQL:

```bash
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

5. Crie o usuário e o banco de dados do projeto:

```bash
sudo -u postgres psql -c "CREATE USER pricewatch WITH PASSWORD 'pricewatch';"
sudo -u postgres psql -c "CREATE DATABASE pricewatch OWNER pricewatch;"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE pricewatch TO pricewatch;"
```

Esses comandos criam:

- Um usuário do banco chamado `pricewatch`
- Uma senha chamada `pricewatch`
- Um banco chamado `pricewatch`

Se aparecer uma mensagem dizendo que o usuário ou banco já existe, pode continuar se você já criou eles antes.

6. Configure as variáveis do backend:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/pricewatch
export DATABASE_USERNAME=pricewatch
export DATABASE_PASSWORD=pricewatch
export JWT_SECRET=pricewatch-secret-key-local-1234567890
export JWT_EXPIRATION_MS=86400000
```

Essas variáveis estão certas se você usou exatamente os comandos do passo 5.

Se você criou o banco com outro nome, usuário ou senha, troque estes valores:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/NOME_DO_SEU_BANCO
export DATABASE_USERNAME=SEU_USUARIO_DO_POSTGRES
export DATABASE_PASSWORD=SUA_SENHA_DO_POSTGRES
export JWT_SECRET=uma-chave-local-com-pelo-menos-32-caracteres
export JWT_EXPIRATION_MS=86400000
```

Para ver os bancos que existem no seu PostgreSQL:

```bash
sudo -u postgres psql -c "\l"
```

Para ver os usuários que existem no seu PostgreSQL:

```bash
sudo -u postgres psql -c "\du"
```

O `JWT_SECRET` não vem do banco. Ele é uma chave usada pelo sistema para gerar o login. Para rodar localmente, pode usar a chave de exemplo acima.

7. Rode o backend:

```bash
mvn spring-boot:run
```

Quando estiver funcionando, a API ficará disponível em:

```text
http://localhost:8080
```

Deixe esse terminal aberto.

8. Abra outro terminal e entre novamente na pasta do projeto:

```bash
cd pricewatch
```

9. Instale as dependências do frontend:

```bash
cd frontend
npm install
```

10. Rode o frontend:

```bash
npm run dev
```

Quando estiver funcionando, o site ficará disponível em:

```text
http://localhost:3000
```

11. Abra o navegador e acesse:

```text
http://localhost:3000
```

Como usar
1. Crie uma conta na tela de cadastro.
2. Entre com seu e-mail e senha.
3. Clique em "Novo produto".
4. Cadastre um produto usando um número de 1 a 20.
5. Depois de cadastrar o produto, o sistema vai abrir a tela de novo alerta.
6. Digite o preço que você quer pagar.
7. Clique em "Criar alerta".
8. Acompanhe o alerta pelo painel.

Como testar a notificação sem esperar
O PriceWatch verifica os preços sozinho a cada 30 minutos.

Para testar mais rápido no seu computador, você pode mandar o sistema fazer essa verificação na hora.

O projeto usa produtos de teste da Fake Store. Na hora de cadastrar o produto, escolha qualquer número de 1 a 20.

Para testar rápido, use um preço-alvo bem alto, por exemplo:

```text
99999
```

Assim o sistema entende que o produto já está abaixo do preço desejado e dispara o alerta quando a verificação rodar.

Depois de criar o alerta, abra outro terminal e rode:

```bash
curl -X POST http://localhost:8080/admin/run-scheduler
```

Esse comando pula a espera de 30 minutos e executa a verificação de preços imediatamente.

Se deu certo, o terminal onde o backend está rodando vai mostrar uma mensagem parecida com esta:

```text
NOTIFICACAO DE PRECO: O produto nome-do-produto atingiu R$ 10.99. Valor alvo: R$ 99999.
```

Depois disso, volte para o site e entre em:

```text
http://localhost:3000/notifications
```

Se tudo funcionou, a notificação do produto vai aparecer nessa tela.

Comandos úteis
Parar o backend ou frontend:

```bash
Ctrl + C
```

Rodar somente os testes do backend:

```bash
cd pricewatch
mvn test
```

Gerar o arquivo final do backend:

```bash
cd pricewatch
mvn clean package
```

Rodar o backend com Docker:

```bash
cd pricewatch
docker build -t pricewatch .
docker run --rm -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/pricewatch \
  -e DATABASE_USERNAME=pricewatch \
  -e DATABASE_PASSWORD=pricewatch \
  -e JWT_SECRET=pricewatch-secret-key-local-1234567890 \
  pricewatch
```

Estrutura do projeto
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
