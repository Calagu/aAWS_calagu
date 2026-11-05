#!/bin/bash
# Copia logica diaria de la base de datos biblioteca (mysqldump + gzip).
# Instalar en /usr/local/sbin y programar con "sudo crontab -e":
#   15 3 * * * /usr/local/sbin/copia_seguridad.sh >> /var/log/copia_biblioteca.log 2>&1
# Debe ejecutarse como root (autenticacion por socket de MySQL, sin contrasena en el script).
set -euo pipefail

DESTINO=/var/backups/biblioteca
DIAS_RETENCION=7
FICHERO="$DESTINO/biblioteca_$(date +%Y%m%d_%H%M%S).sql.gz"

umask 077
mkdir -p "$DESTINO"

# --single-transaction: instantanea coherente de InnoDB sin bloquear a la API
# --databases: el volcado incluye CREATE DATABASE y USE, para restaurar con "mysql" a secas
mysqldump --single-transaction --routines --triggers --databases biblioteca | gzip > "$FICHERO"

# Comprueba que el gzip es valido y no esta vacio
gzip -t "$FICHERO"
[ -s "$FICHERO" ] || { echo "Copia vacia: $FICHERO" >&2; exit 1; }

# Borra copias de mas de N dias
find "$DESTINO" -name 'biblioteca_*.sql.gz' -mtime +"$DIAS_RETENCION" -delete

echo "$(date '+%F %T') copia creada: $FICHERO ($(du -h "$FICHERO" | cut -f1))"
