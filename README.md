# HouseKey Backend

Spring Boot backend foundation for the HouseKey Angular real-estate app.

## Local Run

```powershell
docker compose up -d --build
```

This starts both the Spring Boot API and Postgres in Docker. The API is available at:

```text
http://localhost:8080
```

If you want to run the API directly on your machine while keeping only Postgres in Docker:

```powershell
docker compose up -d postgres
$env:HOUSEKEY_JWT_SECRET='replace-with-at-least-32-random-bytes'
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

## Tests

```powershell
mvn test
```

Tests run with the `test` profile and an H2 database in PostgreSQL compatibility mode.

## Authentication

Local/test profiles seed a demo agency account:

```text
username: agency.demo
password: Password123!
```

Auth endpoints:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/users/me
```
