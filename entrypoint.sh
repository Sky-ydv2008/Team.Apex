#!/bin/sh
set -e

if [ -n "$DATABASE_URL" ]; then
  HOST_DB=$(echo "$DATABASE_URL" | sed -E "s#^postgres(ql)?://[^@]+@(.*)#\2#")
  USER_PASS=$(echo "$DATABASE_URL" | sed -E "s#^postgres(ql)?://([^@]+)@.*#\2#")
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${HOST_DB}?sslmode=prefer"
  export SPRING_DATASOURCE_USERNAME=$(echo "$USER_PASS" | cut -d: -f1)
  export SPRING_DATASOURCE_PASSWORD=$(echo "$USER_PASS" | cut -d: -f2-)
  echo "Connecting to PostgreSQL database at $HOST_DB"
else
  echo "No DATABASE_URL provided — running with embedded H2 database"
fi

exec java -jar /app/api.jar "$@"
