<p align="center">
  <img src="docs/images/banner-icon-racha-manager.png" alt="Racha Manager" width="1473">
</p>

<h1 align="center">Racha Manager</h1>

<p align="center">
API REST para gerenciamento inteligente de equipes, jogadores e partidas esportivas amadoras.
</p>
<p align="center">

  ![Java](https://img.shields.io/badge/Java-21-FFD700?style=for-the-badge&logo=openjdk&logoColor=FFD700)
  ![Spring Boot](https://img.shields.io/badge/spring_boot-4.0.5-6DB33F?style=for-the-badge&logo=springboot)
  ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-ORM-6DB33F?style=for-the-badge&logo=spring)
  ![PostgreSQL](https://img.shields.io/badge/postgresql-database-blue?style=for-the-badge&logo=postgresql)
  ![Flyway](https://img.shields.io/badge/flyway-database_migrations-CC0200?style=for-the-badge)
  ![Docker](https://img.shields.io/badge/docker-containerization-2496ED?style=for-the-badge&logo=docker)
  ![JUnit](https://img.shields.io/badge/JUnit_5-Testing-25A162?style=for-the-badge&logo=junit5&logoColor=25A162)
  ![Mockito](https://img.shields.io/badge/Mockito-Mocking-red?style=for-the-badge)
  ![Coverage](https://img.shields.io/badge/Coverage-85%25%2B-F57C00?style=for-the-badge)
  ![Swagger](https://img.shields.io/badge/swagger-api--docs-green?style=for-the-badge&logo=swagger)
  ![Prometheus](https://img.shields.io/badge/Prometheus-Monitoring-E6522C?style=for-the-badge&logo=prometheus&logoColor=E6522C)
  ![Grafana](https://img.shields.io/badge/Grafana-Observability-F46800?style=for-the-badge&logo=grafana&logoColor=F46800)  
  ![Oracle Cloud](https://img.shields.io/badge/Oracle_Cloud-Cloud-F80000?style=for-the-badge&logo=oracle&logoColor=F80000)

<p align="center">

---

# 📌 Sobre o projeto

O **Racha Manager** é uma API REST desenvolvida para automatizar a organização de "rachas" ou "peladas" de futebol, eliminando a necessidade de controlar manualmente **jogadores, equipes, filas de espera** e **rodadas** durante uma sessão.

A aplicação concentra toda a **lógica de gerenciamento** da partida, permitindo que jogadores sejam adicionados ou removidos dinamicamente enquanto o sistema administra automaticamente a **formação das equipes**, a **fila de espera**, a **rotação dos times** e o **andamento das partidas**.

O projeto nasceu para resolver um problema recorrente em jogos recreativos: organizar **quem joga, quem espera, quem entra, quem sai** e como manter **partidas equilibradas** sem depender de decisões manuais durante toda a sessão.

Além de solucionar o problema de negócio, o projeto foi concebido como um estudo aprofundado em **engenharia de software**, priorizando **arquitetura orientada ao domínio (DDD), boas práticas de desenvolvimento, testes automatizados, observabilidade, conteinerização** e **publicação em ambiente de produção** utilizando **Oracle Cloud Infrastructure (OCI)**.

---

# 🏛️ Arquitetura

O **Racha Manager** foi projetado seguindo princípios de **Domain-Driven Design (DDD)**, buscando manter a lógica de negócio isolada da infraestrutura e modelar o domínio da forma mais próxima possível do problema real.

Neste projeto, a **Session** atua como **Aggregate Root**, sendo responsável por coordenar toda a sessão de jogo e garantir a consistência entre equipes, partidas e fila de espera.

Diferentemente de aplicações CRUD tradicionais, **apenas os jogadores são persistidos em banco de dados**. Todo o restante do estado da partida existe apenas enquanto a sessão está ativa em memória, tornando a aplicação mais simples, performática e preparada para futuras evoluções.

---

## Modelo de Domínio

<p align="center">
  <img src="docs/images/core-domain-model.png" width="900">
</p>

O modelo de domínio foi construído tendo a **Session** como entidade central, responsável por:

- Gerenciar os jogadores ativos da sessão;
- Organizar automaticamente as equipes;
- Controlar a fila de espera;
- Coordenar a partida em andamento;
- Garantir a consistência de toda a sessão de jogo.

Essa abordagem mantém a lógica de negócio concentrada em um único ponto, reduzindo acoplamento entre objetos e facilitando futuras evoluções do domínio.

---

## Estratégia de Persistência

<p align="center">
  <img src="docs/images/persistence-strategy.png" width="900">
</p>

Uma das principais decisões arquiteturais deste projeto foi separar claramente o que pertence à **persistência** do que pertence apenas ao **estado de execução da aplicação**.

| Persistido | Runtime   |
|-------------|-----------|
| ✅ Player | ✅ Session |
| | ✅ Team    |
| | ✅ Match   |
| | ✅ Queue   |

Essa estratégia oferece diversas vantagens:

- 🚀 Criação instantânea de novas sessões;
- ⚡ Manipulação extremamente rápida do estado da partida;
- 🧠 Modelo de domínio mais limpo e coeso;
- 📦 Banco de dados utilizado apenas para informações permanentes;
- 🔄 Facilidade para evoluir futuramente utilizando soluções como **Redis** para distribuição do estado da sessão.

### 💡 Por que essa decisão?

O foco da versão atual é o **gerenciamento de partidas em tempo real**, e não o armazenamento de histórico.

Como sessões, equipes, partidas e filas possuem um ciclo de vida temporário, persistir essas estruturas aumentaria significativamente a complexidade da aplicação sem gerar benefícios para os requisitos atuais.

Essa decisão mantém o domínio enxuto, facilita a manutenção do código e permite que novas funcionalidades sejam adicionadas futuramente sem necessidade de grandes mudanças arquiteturais.

---

# 🧵 Concorrência

Como múltiplas requisições podem tentar alterar a **mesma sessão** ao mesmo tempo (por exemplo, dois jogadores entrando simultaneamente), o `Session` é protegido contra **race conditions** usando `synchronized` nos *use cases* que executam o fluxo `buscar → decidir → alterar → salvar`.

Essa proteção foi validada com **testes de concorrência reais**, que disparam múltiplas threads simultâneas contra a mesma sessão (usando `ExecutorService` + `CountDownLatch`) e garantem que o estado final permanece consistente — sem jogadores duplicados, sem times criados em duplicidade e sem exceptions inesperadas.

> 💡 Essa solução é intencionalmente pensada para a versão atual (estado em memória, instância única). Na evolução com **Redis** (ver Roadmap), a estratégia de lock será migrada para um **distributed lock**, já que `synchronized` não protege estado compartilhado entre múltiplas instâncias da aplicação.

---

# ⚙️ Como rodar localmente

### Pré-requisitos
- Java 21
- Docker e Docker Compose
- Maven (ou use o `./mvnw` incluso no projeto)

### 1. Clone o repositório
```bash
git clone https://github.com/fcolucasvieira/racha-manager.git
cd racha-manager
```

### 2. Configure as variáveis de ambiente
Copie o arquivo de exemplo e ajuste se necessário:
```bash
cp .env.example .env
```
Variáveis utilizadas:

| Variável      | Descrição                       | Valor padrão      |
|---------------|----------------------------------|--------------------|
| `DB_PORT`     | Porta do PostgreSQL              | `5432`             |
| `DB_NAME`     | Nome do banco de dados           | `racha_manager`    |
| `DB_USER`     | Usuário do banco                 | `postgres`         |
| `DB_PASSWORD` | Senha do banco                   | `postgres`         |

### 3. Suba o banco de dados
```bash
docker compose up -d postgres
```

### 4. Rode a aplicação
```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

### 5. (Opcional) Suba a stack completa com observabilidade
```bash
docker compose --profile production up -d
```
Isso sobe a aplicação, o PostgreSQL, o Prometheus (`http://localhost:9090`) e o Grafana (`http://localhost:3000`).

### Rodando os testes
```bash
./mvnw test
```

---

# 📡 Endpoints principais

Documentação interativa completa disponível via Swagger UI (link no topo deste README, ou em `/swagger-ui/index.html` ao rodar localmente).

### Sessões

| Método   | Endpoint                                   | Descrição                                    |
|----------|---------------------------------------------|-----------------------------------------------|
| `POST`   | `/sessions`                                 | Cria uma nova sessão de jogo                  |
| `GET`    | `/sessions/{sessionId}`                     | Consulta o estado atual de uma sessão         |
| `POST`   | `/sessions/{sessionId}/players/{playerId}`  | Adiciona um jogador à sessão                  |
| `DELETE` | `/sessions/{sessionId}/players/{playerId}`  | Remove um jogador da sessão                   |
| `POST`   | `/sessions/{sessionId}/finish-match`        | Finaliza a partida em andamento (vitória/empate) |

### Jogadores

| Método | Endpoint    | Descrição                          |
|--------|-------------|--------------------------------------|
| `POST` | `/players`  | Cadastra um novo jogador             |
| `GET`  | `/players`  | Lista os jogadores cadastrados (paginado) |

---

# 🔄 Fluxo de uma sessão completa

1. **Criar a sessão** — `POST /sessions`, retorna o `sessionId`.
2. **Adicionar jogadores** — `POST /sessions/{sessionId}/players/{playerId}`, um a um. Ao atingir 8 jogadores ativos, os times iniciais são formados automaticamente e a primeira partida começa.
3. **Jogadores extras entram na fila de espera** — a partir do 9º jogador, novos times são montados e aguardam na fila, com prioridade para times que ainda não jogaram.
4. **Finalizar a partida** — `POST /sessions/{sessionId}/finish-match`, informando o time vencedor (ou empate). O time vencedor permanece em quadra, o perdedor vai para o fim da fila, e o próximo time da fila entra automaticamente.
5. **Jogadores podem sair a qualquer momento** — `DELETE /sessions/{sessionId}/players/{playerId}`. Se o time ficar incompleto, o sistema tenta completá-lo automaticamente com jogadores da fila de espera.
6. **Consultar o estado a qualquer momento** — `GET /sessions/{sessionId}`, retornando jogadores ativos, times, partida atual e fila de espera.

---

# 🗺️ Roadmap (V2)

- 🔐 Autenticação e autorização
- 🧠 Redis para persistência distribuída do estado da sessão (com TTL de 24h)
- 🔒 Distributed lock (Redisson) substituindo o `synchronized` local, para suportar múltiplas instâncias
- 🧹 Expiração automática de sessões inativas
- ⚽ Generalização das regras para N times e M jogadores por time (hoje fixo em 4x4)
- 🤖 Pipeline de CI (GitHub Actions) rodando os testes a cada push

---

# 🧪 Qualidade e Testes

O projeto conta com cobertura de testes acima de 90% (JaCoCo), incluindo:
- Testes unitários de modelo de domínio, serviços e *use cases*;
- Testes de integração dos controllers;
- **Testes de concorrência** com múltiplas threads reais, validando a proteção contra *race conditions* no `Session`.