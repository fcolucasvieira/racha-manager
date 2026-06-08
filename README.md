# ⚽ Racha Manager

![Java](https://img.shields.io/badge/java-21-red?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/spring_boot-brightgreen?style=for-the-badge&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/postgresql-database-blue?style=for-the-badge&logo=postgresql)
![JUnit 5](https://img.shields.io/badge/JUnit_5-Testing-25A162?style=for-the-badge\&logo=junit5)
![Mockito](https://img.shields.io/badge/Mockito-Unit_Tests-8BC34A?style=for-the-badge)
![JaCoCo](https://img.shields.io/badge/Coverage-90%25%2B-F57C00?style=for-the-badge)
![Swagger](https://img.shields.io/badge/swagger-api--docs-green?style=for-the-badge&logo=swagger)

---

# 📌 Sobre o projeto

O **Racha Manager** é uma API backend desenvolvida para automatizar a organização de partidas de futebol amador (**racha**), eliminando a necessidade de controle manual de equipes, filas e rodadas.

A aplicação foi projetada para administrar todo o ciclo de uma sessão de jogo, permitindo que jogadores sejam adicionados ou removidos dinamicamente enquanto o sistema gerencia automaticamente a formação de equipes e o fluxo das partidas.

O projeto nasceu para resolver um problema comum em jogos recreativos: **quem joga, quem espera, quem entra, quem sai e como manter partidas equilibradas sem intervenção manual.**

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
