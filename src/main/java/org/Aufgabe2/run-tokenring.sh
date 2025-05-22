#!/bin/bash
PROJECT_DIR="$(cd "$(dirname "$0")/../../../" && pwd)"
CONFIG_FILE="${PROJECT_DIR}/src/main/java/org/Aufgabe2/config.txt"
CLASS_DIR="${PROJECT_DIR}/target/classes"

echo "Projektstamm: ${PROJECT_DIR}"
echo "Konfigurationsdatei: ${CONFIG_FILE}"
echo "Zielverzeichnis für Klassen: ${CLASS_DIR}"

# Prüfe, ob die Konfigurationsdatei existiert
if [ ! -f "${CONFIG_FILE}" ]; then
    echo "❌ Fehler: Die Konfigurationsdatei ${CONFIG_FILE} existiert nicht!"
    exit 1
fi

mkdir -p "${CLASS_DIR}"

echo "🛑 Beende vorherige TokenRing-Prozesse..."
pkill -f "org.Aufgabe2.TokenRingProcess"
sleep 2

echo "🔨 Kompiliere das Java-Projekt..."
javac -d "${CLASS_DIR}" $(find "${PROJECT_DIR}/src/main/java/org/Aufgabe2" -name "*.java")

if [ ! -f "${CLASS_DIR}/org/Aufgabe2/TokenRingProcess.class" ]; then
    echo "❌ Fehler: Die Kompilierung ist fehlgeschlagen!"
    exit 1
fi

echo "✅ Kompilierung erfolgreich!"

# Ermittelt die eigene IP-Adresse
MY_IP=$(hostname -I | awk '{print $1}')
echo "🖥️ Eigene IP-Adresse: ${MY_IP}"

# Prüft, ob die eigene IP in config.txt enthalten ist und bestimmt die Prozess-ID
PROCESS_ID=0
FOUND=false

while read -r line; do
    if [[ "$line" == "$MY_IP" ]]; then
        FOUND=true
        break
    fi
    ((PROCESS_ID++))
done < "${CONFIG_FILE}"

if [ "$FOUND" = false ]; then
    echo "❌ Fehler: Eigene IP-Adresse nicht in config.txt gefunden!"
    exit 1
fi

echo "🚀 Starte TokenRing-Prozess mit ID ${PROCESS_ID}..."
java -cp "${CLASS_DIR}" org.Aufgabe2.TokenRingProcess "$PROCESS_ID"
