#!/bin/bash
# Prueba de restauracion (E13). Se ejecuta DENTRO de la instancia, como ubuntu.
#   bash ~/prueba_restauracion.sh
# Secuencia: crear socio -> copia -> borrar socio -> restaurar -> comprobar.
set -uo pipefail
API=http://127.0.0.1:8080
H='X-Alumno: calagu'
EMAIL="prueba.restauracion.$(date +%s)@example.com"

echo "== 1. Creo un socio de prueba con la API =="
RESP=$(curl -s -X POST "$API/api/socios" -H "Content-Type: application/json" -H "$H" \
       -d "{\"nombre\":\"Socio Restauracion\",\"email\":\"$EMAIL\"}")
echo "$RESP"
ID=$(echo "$RESP" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
[ -n "$ID" ] || { echo "No se pudo crear el socio"; exit 1; }
echo "id del socio: $ID"

echo; echo "== 2. Ejecuto la copia =="
sudo /usr/local/sbin/copia_seguridad.sh || exit 1
FICHERO=$(sudo bash -c 'ls -1t /var/backups/biblioteca/biblioteca_*.sql.gz | head -1')
sudo ls -l "$FICHERO"

echo; echo "== 3. Borro el socio con la API (espero 204) =="
curl -s -o /dev/null -w "DELETE /api/socios/$ID -> HTTP %{http_code}\n" -X DELETE "$API/api/socios/$ID" -H "$H"
curl -s -o /dev/null -w "GET /api/socios/$ID tras borrar -> HTTP %{http_code} (espero 404)\n" "$API/api/socios/$ID" -H "$H"

echo; echo "== 4. Restauro la copia =="
sudo cat "$FICHERO" | gunzip | sudo mysql && echo "restauracion ejecutada"

echo; echo "== 5. Compruebo con la API que el socio ha vuelto (espero 200) =="
curl -s -w "\nGET /api/socios/$ID -> HTTP %{http_code}\n" "$API/api/socios/$ID" -H "$H"

echo; echo "== Limpieza: borro el socio de prueba =="
curl -s -o /dev/null -w "DELETE /api/socios/$ID -> HTTP %{http_code}\n" -X DELETE "$API/api/socios/$ID" -H "$H"
