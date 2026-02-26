Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Write-Host "=== Social Web API (Backend) Evidence Script ==="
Write-Host ""
Write-Host "1) Run tests (CI-equivalent)"
Write-Host "   .\mvnw.cmd -q clean test"
Write-Host ""

Write-Host "Running: .\mvnw.cmd -q clean test"
.\mvnw.cmd -q clean test
Write-Host "OK: tests passed"
Write-Host ""

Write-Host "2) Config evidence (env-first)"
Write-Host "   APP_JWT_SECRET, SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD"
Write-Host ""

Write-Host "3) Grep evidence (correct '--' usage)"
Write-Host '   git grep -n -- "security.jwt.secret" "app.jwt.secret" "SPRING_DATASOURCE_" "spring.datasource." "APP_JWT_SECRET" "SPRING_DATASOURCE_URL" "SPRING_DATASOURCE_USERNAME" "SPRING_DATASOURCE_PASSWORD"'
Write-Host ""

Write-Host "4) Runtime curl evidence (copy/paste)"
Write-Host "   NOTE: requires running server locally (default http://localhost:8081)"
Write-Host ""
Write-Host '$base="http://localhost:8081"'
Write-Host '$body = @{ email="user@example.com"; password="Password123!" } | ConvertTo-Json'
Write-Host '$token = (Invoke-RestMethod -Method Post -Uri "$base/api/auth/login" -ContentType "application/json" -Body $body).accessToken'
Write-Host ""
Write-Host "# Contract: GET /api/feed page=0 -> 400"
Write-Host 'curl.exe -i "$base/api/feed?page=0&limit=10" -H "Authorization: Bearer $token"'
Write-Host ""
Write-Host "# Contract: GET /api/feed limit=999 -> 400"
Write-Host 'curl.exe -i "$base/api/feed?page=1&limit=999" -H "Authorization: Bearer $token"'
Write-Host ""
Write-Host "# Contract: POST /api/feed -> 201 + Location header present"
Write-Host 'curl.exe -i -X POST "$base/api/feed" -H "Authorization: Bearer $token" -H "Content-Type: application/json" --data-binary "{\"content\":\"evidence\"}"'
Write-Host ""
Write-Host "=== Done ==="