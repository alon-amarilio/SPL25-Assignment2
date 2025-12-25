#!/usr/bin/env bash
set -euo pipefail

# Determine repo root relative to this script
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

THREAD_COUNT="${1:-4}"
echo "Starting Validation Process..."

# הכנת תיקיות
OUT_DIR="${ROOT_DIR}/script_output"
mkdir -p "$OUT_DIR"
find "$OUT_DIR" -mindepth 1 -maxdepth 1 -exec rm -rf {} +

if command -v mvn >/dev/null 2>&1; then
  echo "1. Compiling and collecting dependencies..."
  mvn -q clean compile dependency:copy-dependencies -DoutputDirectory=target/lib -DskipTests
  
  if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
      CP="target/classes;target/lib/*"
  else
      CP="target/classes:target/lib/*"
  fi
else
  echo "Error: Maven not found!"
  read -p "Press Enter to exit..."
  exit 1
fi

matches=0
failures=0
declare -a reports=()

echo "2. Running tests on Examples..."

for file in Examples/example*.json; do
  [[ -e "$file" ]] || continue
  fname="${file##*/}"
  index="${fname#example}"
  index="${index%.json}"
  out_file="${OUT_DIR}/output${index}test.json"
  diff_file="${OUT_DIR}/output${index}.diff"

  set +e
  java -cp "$CP" spl.lae.Main "$THREAD_COUNT" "$file" "$out_file"
  rc=$?
  set -e

  if [[ $rc -ne 0 ]]; then
    reports+=("example${index}: CRASHED (Run Failed)")
    failures=$((failures+1))
    continue
  fi

  # בדיקה האם הפלט זהה לתוצאה הצפויה
  if diff -u -w --strip-trailing-cr "Examples/out${index}.json" "$out_file" > "$diff_file" 2>&1; then
    reports+=("V example${index}: SUCCESS (Match)")
    matches=$((matches+1))
    rm -f "$diff_file"
  else
    reports+=("X example${index}: FAILED (Difference found)")
    failures=$((failures+1))
  fi
done

# הדפסת הסיכום למסך
echo -e "\n======================================="
echo "           TEST RESULTS SUMMARY"
echo "======================================="
for r in "${reports[@]}"; do
  echo " ${r}"
done
echo "---------------------------------------"
echo " Total Matches: ${matches}"
echo " Total Failures: ${failures}"
echo "======================================="

if [ $failures -eq 0 ]; then
    echo "GREAT JOB! All tests passed."
else
    echo "Check the 'script_output' folder to see the differences (.diff files)."
fi

echo -e "\n"
# פקודת הקסם שמונעת מהחלון להיסגר:
read -n 1 -s -r -p "Press any key to close this window..."