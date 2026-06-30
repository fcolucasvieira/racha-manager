<p align="center">
  <img src="docs/images/banner-icon-racha-manager.png" alt="Racha Manager" width="900">
</p>

<h1 align="center">Racha Manager</h1>

<p align="center">
API REST para gerenciamento inteligente de equipes, jogadores e partidas esportivas amadoras.
</p>

<p align="center">
  <!-- Plataforma -->
  <img src="https://img.shields.io/badge/Java-21-FFD700?style=for-the-badge&logo=openjdk&logoColor=FFD700"/>
  <img src="https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=6DB33F"/>
  <img src="https://img.shields.io/badge/Spring_Data_JPA-ORM-6DB33F?style=for-the-badge&logo=spring&logoColor=6DB33F"/>

  <!-- Persistência -->
  <img src="https://img.shields.io/badge/PostgreSQL-Database-316192?style=for-the-badge&logo=postgresql&logoColor=316192"/>

  <!-- Infraestrutura -->
  <img src="https://img.shields.io/badge/Docker-Containers-2496ED?style=for-the-badge&logo=docker&logoColor=2496ED"/>
  <img src="https://img.shields.io/badge/Oracle_Cloud-OCI-F80000?style=for-the-badge&logo=oracle&logoColor=F80000"/>

  <!-- Qualidade -->
  <img src="https://img.shields.io/badge/JUnit_5-Testing-25A162?style=for-the-badge&logo=junit5&logoColor=25A162"/>
  <img src="https://img.shields.io/badge/Mockito-Mocking-78A641?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Coverage-90%25%2B-F57C00?style=for-the-badge"/>

  <!-- Documentação -->
  <img src="https://img.shields.io/badge/Swagger-API_Docs-85EA2D?style=for-the-badge&logo=swagger&logoColor=85EA2D"/>

  <!-- Observabilidade -->
  <img src="https://img.shields.io/badge/Prometheus-Monitoring-E6522C?style=for-the-badge&logo=prometheus&logoColor=E6522C"/>
  <img src="https://img.shields.io/badge/Grafana-Observability-F46800?style=for-the-badge&logo=grafana&logoColor=F46800"/>
</p>

---

<p align="center">

## 🚀  Aplicação em Produção

🔗  [Swagger](http://147.15.45.10:8080/swagger-ui/index.html)

</p>

---

# 📌 Sobre o projeto

O **Racha Manager** é uma **API REST** desenvolvida para automatizar a organização de **partidas esportivas amadoras**, eliminando a necessidade de controlar manualmente **jogadores, equipes, filas de espera** e **rodadas** durante uma sessão.

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

| Persistido no PostgreSQL | Mantido apenas em memória |
|---------------------------|---------------------------|
| ✅ Player | ✅ Session |
| | ✅ Team |
| | ✅ Match |
| | ✅ Queue |

Essa estratégia oferece diversas vantagens:

- 🚀 Criação instantânea de novas sessões;
- ⚡ Manipulação extremamente rápida do estado da partida;
- 🧠 Modelo de domínio mais limpo e coeso;
- 📦 Banco de dados utilizado apenas para informações permanentes;
- 🔄 Facilidade para evoluir futuramente utilizando soluções como **Redis** para distribuição do estado da sessão.

### Por que essa decisão?

O foco da versão atual é o **gerenciamento de partidas em tempo real**, e não o armazenamento de histórico.

Como sessões, equipes, partidas e filas possuem um ciclo de vida temporário, persistir essas estruturas aumentaria significativamente a complexidade da aplicação sem gerar benefícios para os requisitos atuais.

Essa decisão mantém o domínio enxuto, facilita a manutenção do código e permite que novas funcionalidades sejam adicionadas futuramente sem necessidade de grandes mudanças arquiteturais.