#!/bin/bash
set -euo pipefail

LOGFILE="spring.log"

# Start Spring Boot in the background, redirect logs
mvn spring-boot:run > "$LOGFILE" 2>&1 &
PID=$!

# Function to clean up when script exits
cleanup() {
  echo "Stopping Spring Boot (PID: $PID)..."
  kill $PID 2>/dev/null || true
}
trap cleanup EXIT

# Show logs in real time
tail -f "$LOGFILE" &
TAIL_PID=$!

# Wait until the /v3/api-docs endpoint responds (max 60s)
echo "Waiting for Spring Boot to start..."
TIMEOUT=60
ELAPSED=0
until curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/v3/api-docs | grep -q "200"; do
    sleep 2
    ELAPSED=$((ELAPSED+2))
    if [ $ELAPSED -ge $TIMEOUT ]; then
        echo "ERROR: Spring Boot did not start within $TIMEOUT seconds."
        kill $TAIL_PID
        exit 1
    fi
done

# Stop tail once service is up
kill $TAIL_PID
echo "Spring Boot is up."

# Ensure docs folder exists
mkdir -p docs

# Fetch OpenAPI docs
curl -s http://localhost:8080/v3/api-docs -o docs/openapi.json
echo "OpenAPI docs saved to docs/openapi.json"
