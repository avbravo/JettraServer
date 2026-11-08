#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JMETER_BIN="/home/avbravo/apache-jmeter-5.6.3/bin/jmeter"
CSV_FILE="${1:-/home/avbravo/apache-jmeter-5.6.3/bin/jmeter_summary_results.csv}"
OUTPUT_DIR="$DIR/reporte_dashboard_html"

if [ ! -f "$CSV_FILE" ]; then
    echo "Error: No se encontró el archivo de resultados CSV: $CSV_FILE"
    echo "Uso: ./generate_dashboard.sh [ruta_al_archivo_csv]"
    exit 1
fi

echo "=========================================================="
echo " Generando Dashboard HTML Interactivo de Métricas JMeter"
echo " Archivo de origen: $CSV_FILE"
echo " Directorio destino: $OUTPUT_DIR"
echo "=========================================================="

rm -rf "$OUTPUT_DIR"
"$JMETER_BIN" -g "$CSV_FILE" -o "$OUTPUT_DIR"

echo ""
echo "✅ Dashboard generado exitosamente."
echo "Puedes abrirlo en tu navegador con:"
echo "   xdg-open \"$OUTPUT_DIR/index.html\""
echo "o mediante URL de archivo:"
echo "   file://$OUTPUT_DIR/index.html"
