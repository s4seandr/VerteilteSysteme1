#!/bin/bash
PROJECT_DIR="$(cd "$(dirname "$0")/../../../" && pwd)"
SRC_DIR="${PROJECT_DIR}/java/org/Aufgabe1"
CLASS_DIR="${PROJECT_DIR}/target/classes"

echo "Projektstamm: ${PROJECT_DIR}"
echo "Quellcodeverzeichnis: ${SRC_DIR}"
echo "Zielverzeichnis für Klassen: ${CLASS_DIR}"

if [ ! -d "${SRC_DIR}" ]; then
    echo "❌ Fehler: Das Quellcode-Verzeichnis ${SRC_DIR} existiert nicht!"
    exit 1
fi

mkdir -p "${CLASS_DIR}"

echo "🛑 Beende vorherige TokenRing-Prozesse..."
pkill -f "org.Aufgabe1.TokenRingProcess"
sleep 2

echo "🔨 Kompiliere das Java-Projekt..."
javac -d "${CLASS_DIR}" $(find "${SRC_DIR}" -name "*.java")

if [ ! -f "${CLASS_DIR}/org/Aufgabe1/TokenRingProcess.class" ]; then
    echo "❌ Fehler: Die Kompilierung ist fehlgeschlagen!"
    exit 1
fi

echo "✅ Kompilierung erfolgreich!"

startTokenRing() {
    local n=$1
    echo "------------------------------------------------------"
    echo "🚀 Starte TokenRing mit ${n} Prozessen..."
    pids=()

    for (( i=0; i<n; i++ )); do
        java -cp "${CLASS_DIR}" org.Aufgabe1.TokenRingProcess "$i" "$n" "1.0" &
        pids+=($!)
        sleep 0.2
    done

    sleep $(( n * 5 ))

    echo "🛑 Beende TokenRing-Prozesse..."
    for pid in "${pids[@]}"; do
        kill "$pid" 2>/dev/null
    done
    sleep 2
    wait
}

n=2
while true; do
    startTokenRing "$n"
    n=$(( n * 2 ))
done
