# Social Web API (Backend)

## Requirements
- Java 21
- PostgreSQL (dev/prod)
- Maven Wrapper (included)

## Configuration (env-first)
The backend reads configuration from environment variables first.

### Database
- `SPRING_DATASOURCE_URL` (default: `jdbc:postgresql://localhost:5433/social_web`)
- `SPRING_DATASOURCE_USERNAME` (default: `social_web_app`)
- `SPRING_DATASOURCE_PASSWORD` (default: `password`)

### JWT
- `APP_JWT_SECRET` (default is dev-only; set in prod)
- `app.jwt.issuer` default: `social-web-api`
- `app.jwt.ttlSeconds` default: `3600`

## Run (dev)
From `backend/social-web-api`:

```powershell
$env:APP_JWT_SECRET="replace-with-a-long-random-secret-at-least-32-chars"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/social_web"
$env:SPRING_DATASOURCE_USERNAME="social_web_app"
$env:SPRING_DATASOURCE_PASSWORD="password"
.\mvnw.cmd spring-boot:run
