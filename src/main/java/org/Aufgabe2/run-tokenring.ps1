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

# Eigene IP-Adresse abrufen
$MY_IP = (Get-NetIPAddress | Where-Object { $_.AddressFamily -eq 'IPv4' -and $_.InterfaceAlias -match 'Wi-Fi|Ethernet' }).IPAddress
Write-Host "🖥️ Eigene IP-Adresse: $MY_IP"

# Prozess-ID ermitteln
$PROCESS_ID = 0
$FOUND = $false

foreach ($line in Get-Content $CONFIG_FILE) {
    if ($line -eq $MY_IP) {
        $FOUND = $true
        break
    }
    $PROCESS_ID++
}

if (-Not $FOUND) {
    Write-Host "❌ Fehler: Eigene IP-Adresse nicht in config.txt gefunden!"
    Exit 1
}

Write-Host "🚀 Starte TokenRing-Prozess mit ID $PROCESS_ID..."
Start-Process -NoNewWindow -FilePath "java" -ArgumentList "-cp $CLASS_DIR org.Aufgabe2.TokenRingProcess $PROCESS_ID"
