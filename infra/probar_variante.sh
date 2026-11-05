#!/usr/bin/env bash
# Comprobaciones propias de la VARIANTE 1 (max. 3 prestamos, 14 dias, informe mas-prestados).
# Uso:  bash probar_variante.sh http://127.0.0.1:8080 calagu
# Requiere curl, jq y date de GNU. Pegar tambien al final de probar_api.sh para la evidencia E14.
BASE="${1:-http://127.0.0.1:8080}"
ALUMNO="${2:-calagu}"
MAXIMO=3
DIAS=14
OK=0
FALLO=0
TMP=$(mktemp)
trap 'rm -f "$TMP" "$TMP.h"' EXIT

comprobar() {   # descripcion esperado obtenido
    if [ "$2" = "$3" ]; then
        echo "  OK    $1"; OK=$((OK + 1))
    else
        echo "  FALLA $1 (esperado: $2, obtenido: $3)"; FALLO=$((FALLO + 1))
    fi
}

http() {   # metodo ruta [cuerpo]  -> imprime el codigo HTTP; cuerpo en $TMP, cabeceras en $TMP.h
    if [ -n "$3" ]; then
        curl -s -o "$TMP" -D "$TMP.h" -w '%{http_code}' -X "$1" "$BASE$2" \
            -H 'Content-Type: application/json' --data "$3"
    else
        curl -s -o "$TMP" -D "$TMP.h" -w '%{http_code}' -X "$1" "$BASE$2"
    fi
}

cabecera_alumno() { grep -i '^x-alumno:' "$TMP.h" | tr -d '\r' | awk '{print $2}'; }

echo "== Datos de prueba =="
SUF=$(date +%s)
http POST /api/autores "{\"nombre\":\"Autor variante $SUF\",\"nacionalidad\":\"Prueba\",\"anioNacimiento\":1950}" > /dev/null
AUTOR=$(jq -r .id "$TMP")
LIBROS=()
for i in 1 2 3 4; do
    ISBN=$(printf '9%012d' $((SUF * 10 + i)))
    http POST /api/libros "{\"isbn\":\"$ISBN\",\"titulo\":\"Libro variante $SUF-$i\",\"anioPublicacion\":2000,\"genero\":\"Prueba\",\"ejemplares\":5,\"autorId\":$AUTOR}" > /dev/null
    LIBROS+=("$(jq -r .id "$TMP")")
done
http POST /api/socios "{\"nombre\":\"Socio variante $SUF\",\"email\":\"variante$SUF@example.com\"}" > /dev/null
SOCIO=$(jq -r .id "$TMP")
echo "  autor=$AUTOR libros=${LIBROS[*]} socio=$SOCIO"

echo "== 1. Limite de prestamos abiertos de la variante ($MAXIMO) =="
PRESTAMOS=()
for i in 0 1 2; do
    cod=$(http POST /api/prestamos "{\"libroId\":${LIBROS[$i]},\"socioId\":$SOCIO}")
    comprobar "prestamo $((i + 1)) de $MAXIMO -> 201" 201 "$cod"
    PRESTAMOS+=("$(jq -r .id "$TMP")")
    if [ "$i" = 0 ]; then F1=$(jq -r .fechaPrestamo "$TMP"); F2=$(jq -r .fechaLimite "$TMP"); fi
done
cod=$(http POST /api/prestamos "{\"libroId\":${LIBROS[3]},\"socioId\":$SOCIO}")
comprobar "prestamo $((MAXIMO + 1)) (supera el maximo) -> 409" 409 "$cod"

echo "== 2. Dias de prestamo de la variante ($DIAS) =="
comprobar "fechaLimite = fechaPrestamo + $DIAS dias" "$(date -d "$F1 + $DIAS days" +%F)" "$F2"

echo "== 3. Informe /api/informes/mas-prestados =="
cod=$(http GET /api/informes/mas-prestados)
comprobar "sin parametro -> 200" 200 "$cod"
comprobar "por defecto devuelve como mucho 5 filas" true "$(jq 'length <= 5' "$TMP")"
comprobar "ordenado de mayor a menor numero de prestamos" true \
    "$(jq '[.[].prestamos] == ([.[].prestamos] | sort | reverse)' "$TMP")"
cod=$(http GET "/api/informes/mas-prestados?limite=2")
comprobar "limite=2 -> 200" 200 "$cod"
comprobar "limite=2 devuelve como mucho 2 filas" true "$(jq 'length <= 2' "$TMP")"
comprobar "limite=0 (fuera de rango) -> 400" 400 "$(http GET "/api/informes/mas-prestados?limite=0")"
comprobar "limite=21 (fuera de rango) -> 400" 400 "$(http GET "/api/informes/mas-prestados?limite=21")"
comprobar "limite=abc (no numerico) -> 400" 400 "$(http GET "/api/informes/mas-prestados?limite=abc")"

echo "== 4. Cabecera X-Alumno =="
http GET /api/estado > /dev/null
comprobar "X-Alumno en una respuesta correcta" "$ALUMNO" "$(cabecera_alumno)"
http GET /api/libros/999999 > /dev/null
comprobar "X-Alumno tambien en una respuesta de error (404)" "$ALUMNO" "$(cabecera_alumno)"

echo "== Limpieza: se devuelven los prestamos de prueba =="
for id in "${PRESTAMOS[@]}"; do
    comprobar "devolucion del prestamo $id -> 200" 200 "$(http PUT "/api/prestamos/$id/devolucion")"
done

echo
echo "Resultado variante: $OK correctas, $FALLO fallidas"
[ "$FALLO" -eq 0 ]
