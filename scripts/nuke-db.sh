#!/usr/bin/env bash
# Drops all known tables from the database.
# Usage: ./scripts/nuke-db.sh
# Reads DB_URL, DB_USERNAME, DB_PASSWORD from .env at the repo root.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "ERROR: .env file not found at $ENV_FILE" >&2
  exit 1
fi

# Parse host, port, database from jdbc URL
# Expected format: jdbc:mysql://<host>:<port>/<db>
DB_URL=$(grep '^DB_URL=' "$ENV_FILE" | cut -d= -f2-)
DB_USERNAME=$(grep '^DB_USERNAME=' "$ENV_FILE" | cut -d= -f2-)
DB_PASSWORD=$(grep '^DB_PASSWORD=' "$ENV_FILE" | cut -d= -f2-)

if [[ -z "$DB_URL" || -z "$DB_USERNAME" || -z "$DB_PASSWORD" ]]; then
  echo "ERROR: DB_URL, DB_USERNAME or DB_PASSWORD missing from .env" >&2
  exit 1
fi

# Strip jdbc: prefix
MYSQL_URL="${DB_URL#jdbc:}"
# Extract host:port/db
HOST_PORT_DB="${MYSQL_URL#mysql://}"
HOST="${HOST_PORT_DB%%:*}"
PORT_DB="${HOST_PORT_DB#*:}"
PORT="${PORT_DB%%/*}"
DATABASE="${PORT_DB#*/}"

echo "Nuking all tables in $DATABASE on $HOST:$PORT ..."
echo "This is IRREVERSIBLE. Press Ctrl+C within 5 seconds to abort."
sleep 5

mysql -h "$HOST" -P "$PORT" -u "$DB_USERNAME" -p"$DB_PASSWORD" "$DATABASE" <<'SQL'
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS mystery_submission;
DROP TABLE IF EXISTS notepad_entry;
DROP TABLE IF EXISTS medal;
DROP TABLE IF EXISTS level_completion;
DROP TABLE IF EXISTS question;
DROP TABLE IF EXISTS level;
DROP TABLE IF EXISTS stop;
DROP TABLE IF EXISTS enrollment;
DROP TABLE IF EXISTS classroom_teacher;
DROP TABLE IF EXISTS classroom;
DROP TABLE IF EXISTS user_avatar_unlock;
DROP TABLE IF EXISTS avatar_item;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS app_user;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;
SQL

echo "Done. All tables dropped."
