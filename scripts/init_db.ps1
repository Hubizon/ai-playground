Write-Host "Initializing PostgreSQL..."

psql -U postgres -f ".\db\init_pg.sql"

$env:PGPASSWORD = "aiplayground"

psql -U aiplayground -d aiplayground -f ".\db\clear.sql"
psql -U aiplayground -d aiplayground -f ".\db\create.sql"
psql -U aiplayground -d aiplayground -f ".\db\fill2.sql"
psql -U aiplayground -d aiplayground -f ".\db\triggers.sql"

Remove-Item Env:PGPASSWORD

Write-Host "Done. User: aiplayground | Password: aiplayground | Database: aiplayground"