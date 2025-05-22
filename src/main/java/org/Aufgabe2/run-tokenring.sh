#!/bin/bash
PROJECT_DIR="$(cd "$(dirname "$0")/../../../" && pwd)"
CONFIG_FILE="${PROJECT_DIR}/java/org/Aufgabe2/config.txt"
CLASS_DIR="${PROJECT_DIR}/target/classes"

echo "Projektstamm: ${PROJECT_DIR}"
echo "Konfigurationsdatei: ${CONFIG_FILE}"
echo "Zielverzeichnis für Klassen: ${CLASS_DIR}"

if [ ! -f "${CONFIG_FILE}" ]; then
    echo "❌ Fehler: Die Konfigurationsdatei ${CONFIG_FILE} existiert nicht!"
    exit 1
fi

mkdir -p "${CLASS_DIR}"

echo "🛑 Beende vorherige TokenRing-Prozesse..."
pkill -f "org.Aufgabe2.TokenRingProcess"
sleep 2

echo "🔨 Kompiliere das Java-Projekt..."
javac -d "${CLASS_DIR}" $(find "${PROJECT_DIR}/java/org/Aufgabe2" -name "*.java")

if [ ! -f "${CLASS_DIR}/org/Aufgabe2/TokenRingProcess.class" ]; then
    echo "❌ Fehler: Die Kompilierung ist fehlgeschlagen!"
    exit 1
fi

echo "✅ Kompilierung erfolgreich!"

while read -r ip; do
    java -cp "${CLASS_DIR}" org.Aufgabe2.TokenRingProcess "$ip" "1.0" &
done < "${CONFIG_FILE}"

echo "🚀 Alle Prozesse gestartet!"
