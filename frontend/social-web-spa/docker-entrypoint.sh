#!/bin/sh
set -eu

: "${API_BASE_URL:?API_BASE_URL is required}"

# Replace placeholder in dist/config.js (after build)
# ensure file exists
if [ -f /app/dist/config.js ]; then
  sed -i "s|__API_BASE_URL__|$API_BASE_URL|g" /app/dist/config.js
else
  echo "dist/config.js not found"
  exit 1
fi

exec "$@"