Push-Location (Split-Path $MyInvocation.MyCommand.Definition)

$zipFile = ".\javafx-sdk-17.0.15.zip"
$extractFolder = "..\javafx-sdk-17.0.15"

if (-Not (Test-Path $extractFolder)) {
    Write-Host "Unzipping $zipFile..."
    Expand-Archive -Path $zipFile -DestinationPath ".." -Force
    Write-Host "Unzip complete."
} else {
    Write-Host "$extractFolder already exists. Skipping unzip."
}

Write-Host "Initializing PostgreSQL..."

psql -U postgres -f ".\db\init_pg.sql"

$env:PGPASSWORD = "aiplayground"

psql -U aiplayground -d aiplayground -f ".\db\clear.sql"
psql -U aiplayground -d aiplayground -f ".\db\create.sql"
psql -U aiplayground -d aiplayground -f ".\db\fill2.sql"
psql -U aiplayground -d aiplayground -f ".\db\triggers.sql"

Remove-Item Env:PGPASSWORD

Write-Host "Done. User: aiplayground | Password: aiplayground | Database: aiplayground"

Pop-Location