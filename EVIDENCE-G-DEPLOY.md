# Evidence — Grade G — Deploy Readiness (Backend)

## Docker Compose (API + Postgres)
- Command:
  - `docker compose up --build`
- Result:
  - API container started successfully
  - Postgres container healthy
  - Flyway migrations applied
  - Port: 8081

## Health Check (Actuator)
- URL:
  - `/actuator/health`
- Output:
```json
{"groups":["liveness","readiness"],"status":"UP"}