Write-Host "Initializing PostgreSQL..."

psql -U postgres -f ".\db\init_pg.sql"

psql -U aiplayground -d aiplayground -f ".\db\create.sql"

Write-Host "Done. User: aiplayground | Password: aiplayground | Database: aiplayground"
