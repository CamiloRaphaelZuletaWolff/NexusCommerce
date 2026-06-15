# NexusCommerce — Backend

Spring Boot 3 modular monolith. **Scaffolded in Phase 2** — this folder currently
documents the planned layout so the structure is reviewable from day one.

## Stack

- Java 21 (language level) · Spring Boot 3.x · Maven (+ Maven Wrapper)
- Spring Web · Spring Security · Spring Data JPA · Spring Data MongoDB
- Flyway · Bean Validation · Lombok · MapStruct · springdoc-openapi
- JUnit 5 · Mockito · Testcontainers

## Package-by-feature layout (planned)

```
com.nexuscommerce
├── NexusCommerceApplication.java
├── auth/            # login, registration, JWT issue/verify, refresh
├── users/           # user + role aggregate (PostgreSQL)
├── stores/          # multi-store management (PostgreSQL)
├── catalog/         # product documents, categories, attributes (MongoDB)
├── cart/            # cart metadata (PostgreSQL) + items
├── orders/          # order + order_items, ACID checkout (PostgreSQL)
├── payments/        # payment records, encrypted card fields (PostgreSQL)
├── inventory/       # stock levels, transactional reservations (PostgreSQL)
├── analytics/       # sales reporting, views, subqueries (PostgreSQL)
├── integration/     # UUID-based PostgreSQL <-> MongoDB sync service
├── security/        # Spring Security config, JWT filter, RBAC, encryption
└── shared/          # cross-cutting: DTO base, error handling, audit, config
```

Each feature package follows the same internal shape:

```
<feature>/
├── api/         # @RestController + request/response DTOs
├── domain/      # entities / documents + value objects
├── repository/  # Spring Data interfaces (JPA or Mongo)
├── service/     # business logic, @Transactional boundaries
└── mapper/      # MapStruct entity <-> DTO mappers
```

## Conventions

- Constructor injection only; dependency fields `private final`.
- Controllers are thin; business logic lives in `@Service`.
- Never expose JPA entities over HTTP — always DTOs.
- Validate every request DTO with Bean Validation (`@Valid`).
- Global error handling via `@ControllerAdvice`.
- Schema changes only through Flyway migrations (`src/main/resources/db/migration`).

See [../docs/ARCHITECTURE.md](../docs/ARCHITECTURE.md) for the full design.
