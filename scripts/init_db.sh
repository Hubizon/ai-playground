#!/bin/bash

echo "Initializing PostgreSQL..."

sudo -u postgres psql -f ./db/init_pg.sql

export PGPASSWORD=aiplayground

psql -U aiplayground -d aiplayground -f ./db/clear.sql
psql -U aiplayground -d aiplayground -f ./db/create.sql
psql -U aiplayground -d aiplayground -f ./db/fill2.sql
psql -U aiplayground -d aiplayground -f ./db/triggers.sql

unset PGPASSWORD

echo "Done. User: aiplayground | Password: aiplayground | Database: aiplayground"
