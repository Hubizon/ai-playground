#!/bin/bash

echo "Initializing PostgreSQL..."

sudo -u postgres psql -f ./db/init_pg.sql

psql -U aiplayground -d aiplayground -f ./db/create.sql

echo "Done. User: aiplayground | Password: aiplayground | Database: aiplayground"
