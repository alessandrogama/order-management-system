# 🏗️ Order Management System — Microservices Portfolio

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)
![MongoDB](https://img.shields.io/badge/MongoDB-7-green?logo=mongodb)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-orange?logo=rabbitmq)

Sistema de gestão de pedidos construído com arquitetura de microserviços, demonstrando boas práticas de desenvolvimento enterprise com Java 17 e Spring Boot.

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia |
|--------|------------|
| Backend | Java 17, Spring Boot 3.2, Spring Cloud 2023 |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway + JWT |
| Banco Relacional | PostgreSQL 15 |
| Banco NoSQL | MongoDB 7 |
| Mensageria | RabbitMQ 3 |
| Containerização | Docker + Docker Compose |
| Orquestração | Kubernetes |
| CI/CD | GitHub Actions |
| Testes | JUnit 5, Mockito, Jacoco |
| Docs | Swagger / OpenAPI 3 |

---

## 🏛️ Arquitetura

```
Cliente → API Gateway (8080)
              ↓
        Eureka Server (8761) ← Service Discovery
              ↓
  ┌───────────┬──────────────┬──────────────────┐
  │           │              │                  │
User Service  Product Service  Order Service  Notification Service
(8081/PG)    (8082/Mongo)    (8083/PG)      (8084/RabbitMQ)
```

---

## 🚀 Como Executar

### Pré-requisitos
- Docker Desktop instalado e rodando
- Nenhuma outra instalação necessária!

### 1. Clone o repositório
```bash
git clone https://github.com/SEU_USUARIO/order-management-system.git
cd order-management-system
```

### 2. Suba todos os serviços
```bash
docker-compose up -d
```

O primeiro start demora ~5 minutos pois compila todos os serviços. ☕

### 3. Aguarde os serviços ficarem saudáveis
```bash
docker-compose ps
```

---

## 🔗 URLs dos Serviços

| Serviço | URL |
|---------|-----|
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| RabbitMQ Console | http://localhost:15672 (portfolio/portfolio123) |
| User Service Swagger | http://localhost:8081/swagger-ui.html |
| Product Service Swagger | http://localhost:8082/swagger-ui.html |
| Order Service Swagger | http://localhost:8083/swagger-ui.html |

---

## 📋 Fluxo de Teste via API

### 1. Registrar um usuário
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"João Silva","email":"joao@email.com","password":"senha123"}'
```

### 2. Fazer login e pegar o token JWT
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@email.com","password":"senha123"}'
```

### 3. Criar um produto (usando o token)
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sku":"PROD-001","name":"Notebook Dell","description":"Notebook Dell Inspiron","price":2999.99,"stockQuantity":50,"category":"electronics"}'
```

### 4. Criar um pedido
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":"SEU_USER_ID","items":[{"productId":"ID_DO_PRODUTO","quantity":2}]}'
```

---

## 🏗️ Estrutura do Projeto

```
order-management-system/
├── api-gateway/           # Spring Cloud Gateway + JWT Auth
├── eureka-server/         # Service Discovery
├── user-service/          # Auth (PostgreSQL)
├── product-service/       # Catálogo (MongoDB)
├── order-service/         # Pedidos (PostgreSQL + RabbitMQ)
├── notification-service/  # Notificações (RabbitMQ consumer)
├── k8s/                   # Kubernetes manifests
├── .github/workflows/     # CI/CD Pipeline
├── docker-compose.yml
├── init-db.sql
└── sonar-project.properties
```

---

## 🧪 Rodando os Testes

```bash
# User Service
cd user-service && mvn test

# Order Service
cd order-service && mvn test

# Relatório de cobertura (Jacoco)
mvn verify
# Abre: target/site/jacoco/index.html
```

---

## 📐 Princípios Aplicados

- **Clean Architecture** — Separação em camadas (domain, service, repository, controller)
- **SOLID** — Single Responsibility, Open/Closed, Liskov, Interface Segregation, Dependency Inversion
- **Java 17 Features** — Records para DTOs, var, text blocks
- **Database per Service** — Cada serviço tem seu próprio banco
- **Event-Driven** — Comunicação assíncrona via RabbitMQ

---

## 🛑 Parar os Serviços

```bash
# Parar sem remover dados
docker-compose stop

# Parar e remover tudo (incluindo volumes)
docker-compose down -v
```
