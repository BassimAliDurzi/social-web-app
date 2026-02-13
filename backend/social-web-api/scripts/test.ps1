$ErrorActionPreference = "Stop"

function Get-EnvOrDefault([string]$name, [string]$defaultValue) {
  $val = [System.Environment]::GetEnvironmentVariable($name)
  if ([string]::IsNullOrWhiteSpace($val)) { return $defaultValue }
  return $val
}

function Require-Env([string]$name) {
  $val = [System.Environment]::GetEnvironmentVariable($name)
  if ([string]::IsNullOrWhiteSpace($val)) {
    Write-Host ""
    Write-Host "Missing required env var: $name" -ForegroundColor Red
    exit 1
  }
  return $val
}

$env:SPRING_PROFILES_ACTIVE = "test"

$env:POSTGRES_HOST = Get-EnvOrDefault "POSTGRES_HOST" "localhost"
$env:POSTGRES_PORT = Get-EnvOrDefault "POSTGRES_PORT" "5433"
$env:POSTGRES_DB = Get-EnvOrDefault "POSTGRES_DB" "social_web_test"
$env:POSTGRES_USER = Get-EnvOrDefault "POSTGRES_USER" "postgres"

$pass = [System.Environment]::GetEnvironmentVariable("POSTGRES_PASSWORD")
if ([string]::IsNullOrWhiteSpace($pass)) {
  Write-Host ""
  Write-Host "POSTGRES_PASSWORD is not set. Tests will likely fail to connect." -ForegroundColor Yellow
  Write-Host "Set it for this session, example:" -ForegroundColor Yellow
  Write-Host '  $env:POSTGRES_PASSWORD="your_password"' -ForegroundColor Yellow
  exit 1
}

Write-Host "Running backend tests (profile=test) with:" -ForegroundColor Cyan
Write-Host "POSTGRES_HOST=$($env:POSTGRES_HOST)" -ForegroundColor Gray
Write-Host "POSTGRES_PORT=$($env:POSTGRES_PORT)" -ForegroundColor Gray
Write-Host "POSTGRES_DB=$($env:POSTGRES_DB)" -ForegroundColor Gray
Write-Host "POSTGRES_USER=$($env:POSTGRES_USER)" -ForegroundColor Gray
Write-Host "POSTGRES_PASSWORD=(set)" -ForegroundColor Gray

./mvnw test
