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

## 🌐 Aplicação em Produção

A documentação da API encontra-se disponível publicamente através do Swagger UI.

**http://147.15.45.10:8080/swagger-ui/index.html**

</p>

---

# 🔍 Sobre

O **Racha Manager** é uma **API REST** desenvolvida para automatizar a organização de **partidas esportivas amadoras**, eliminando a necessidade de controlar manualmente **jogadores, equipes, filas de espera** e **rodadas** durante uma sessão.

A aplicação concentra toda a **lógica de gerenciamento** da partida, permitindo que jogadores sejam adicionados ou removidos dinamicamente enquanto o sistema administra automaticamente a **formação das equipes**, a **fila de espera**, a **rotação dos times** e o **andamento das partidas**.

O projeto nasceu para resolver um problema recorrente em jogos recreativos: organizar **quem joga, quem espera, quem entra, quem sai** e como manter **partidas equilibradas** sem depender de decisões manuais durante toda a sessão.

Além de solucionar o problema de negócio, o projeto foi concebido como um estudo aprofundado em **engenharia de software**, priorizando **arquitetura orientada ao domínio (DDD), boas práticas de desenvolvimento, testes automatizados, observabilidade, conteinerização** e **publicação em ambiente de produção** utilizando **Oracle Cloud Infrastructure (OCI)**.

---


# 🎯 Principais funcionalidades

### 👥 Gestão de jogadores

* Entrada e saída dinâmica de jogadores
* Controle de participantes ativos
* Atualização automática das equipes

### ⚖️ Balanceamento de equipes

* Formação inicial automática
* Criação dinâmica de novos times
* Controle de equipes completas e incompletas

### 🏆 Fluxo de partidas

* Encerramento por vitória
* Encerramento por empate
* Rotação automática da fila
* Atualização contínua dos confrontos

### 🔄 Sistema de prioridade

* Reposição automática de equipes em jogo
* Transferência inteligente de jogadores
* Dissolução de equipes vazias

---

# 🏗️ Arquitetura

O projeto segue princípios de:

* **Clean Architecture**
* **Domain-Driven Design (DDD)**
* Evolução para **Hexagonal Architecture**

Estrutura principal:

```text
Controller
    ↓
Use Case
    ↓
Domain Services
    ↓
Ports
    ↓
Persistence Adapters
```

Toda a lógica de negócio está concentrada no domínio, mantendo baixo acoplamento e alta testabilidade.

---

# 🧪 Testes

O projeto possui cobertura automatizada focada principalmente nas regras de negócio.

### Ferramentas

* JUnit 5
* Mockito
* JaCoCo

### Cobertura atual

✅ Serviços de domínio

✅ Casos de uso (Use Cases)

✅ Fluxos críticos da aplicação

📊 **90%+ de cobertura de código**

---

# 🧰 Tecnologias utilizadas

* Java 21
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Maven
* JUnit 5
* Mockito
* JaCoCo

---

# 🚧 Status do projeto

O projeto encontra-se em desenvolvimento ativo.

Próximas etapas:

* Arquitetura Hexagonal
* Docker
* Swagger/OpenAPI
* Deploy em nuvem
* CI/CD

---

# 🚀 Objetivos de aprendizado

Este projeto foi desenvolvido para aprofundar conhecimentos em:

* Modelagem de regras de negócio complexas
* Arquitetura de software
* Testes automatizados
* Boas práticas de desenvolvimento backend
* Sistemas orientados ao domínio

---

# 👨‍💻 Autor

**Lucas Vieira**

Estudante de Engenharia de Computação — UFC Sobral

GitHub:
https://github.com/fcolucasvieira
