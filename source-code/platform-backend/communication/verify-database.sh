#!/bin/bash
# verify-database.sh

echo "=== Verifying Database Configuration ==="
echo "Host: $DB_HOST"
echo "Port: $DB_PORT"
echo "Database: $DB_NAME"
echo "Username: $DB_USERNAME"

# Test connection
echo "Testing database connection..."
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DB_NAME -c "\conninfo"

# List tables after migration
echo "Listing database tables..."
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DB_NAME -c "\dt"

# Check Flyway migration status
echo "Checking Flyway migration status..."
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DB_NAME -c "SELECT version, description, installed_on, success FROM flyway_schema_history ORDER BY installed_rank;"