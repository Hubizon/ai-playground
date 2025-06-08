#!/bin/bash

echo "Initializing PostgreSQL..."

psql -U postgres -f ./scripts/db/init_pg.sql

export PGPASSWORD=aiplayground

psql -U aiplayground -d aiplayground -f ./scripts/db/clear.sql
psql -U aiplayground -d aiplayground -f ./scripts/db/create.sql
psql -U aiplayground -d aiplayground -f ./scripts/db/fill2.sql
psql -U aiplayground -d aiplayground -f ./scripts/db/triggers.sql

unset PGPASSWORD

echo "Done. User: aiplayground | Password: aiplayground | Database: aiplayground"
