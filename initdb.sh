#!/usr/bin/env bash
set -euo pipefail

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-aula_db}"
DB_USER="${DB_USER:-postgres}"
DB_PASS="${DB_PASS:-postgres}"
SQL_FILE="${1:-$(dirname "$0")/initdb.sql}"

if [[ ! -f "$SQL_FILE" ]]; then
  echo "No se encontró el archivo SQL: $SQL_FILE" >&2
  exit 1
fi

export PGPASSWORD="$DB_PASS"

psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$SQL_FILE"
