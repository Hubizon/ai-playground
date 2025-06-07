Write-Host "Initializing PostgreSQL..."

psql -U postgres -f ".\db\init_pg.sql"

psql -U aiplayground -d aiplayground -f ".\db\create.sql"

psql -U aiplayground -d aiplayground -f ".\db\fill.sql"

psql -U aiplayground -d aiplayground -f ".\db\triggers.sql"


Write-Host "Done. User: aiplayground | Password: aiplayground | Database: aiplayground"
