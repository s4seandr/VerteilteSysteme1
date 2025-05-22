$PROJECT_DIR = Resolve-Path "$PSScriptRoot\..\..\.."
$CONFIG_FILE = "$PROJECT_DIR\src\main\java\org\Aufgabe2\config.txt"
$CLASS_DIR = "$PROJECT_DIR\target\classes"

Write-Host "Projektstamm: $PROJECT_DIR"
Write-Host "Konfigurationsdatei: $CONFIG_FILE"
Write-Host "Zielverzeichnis für Klassen: $CLASS_DIR"

if (-Not (Test-Path $CONFIG_FILE)) {
    Write-Host "❌ Fehler: Konfigurationsdatei nicht gefunden!"
    Exit 1
}

New-Item -ItemType Directory -Force -Path $CLASS_DIR

Write-Host "🔨 Kompiliere das Java-Projekt..."
javac -d $CLASS_DIR (Get-ChildItem "$PROJECT_DIR\src\main\java\org\Aufgabe2" -Filter *.java).FullName

foreach ($ip in Get-Content $CONFIG_FILE) {
    Start-Process -NoNewWindow -FilePath "java" -ArgumentList "-cp $CLASS_DIR org.Aufgabe2.TokenRingProcess $ip"
}
