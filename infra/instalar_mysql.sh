#!/usr/bin/env bash
# FASE 3 (en la instancia): instala MySQL, aplica zz-biblioteca.cnf, carga el esquema y crea las dos cuentas.
# Requiere en ~/sql los scripts 01_esquema.sql, 02_datos.sql y 04_verificacion.sql de los materiales.
# Las contrasenas se piden por teclado (no se muestran ni se guardan en ningun fichero).
set -euo pipefail

sudo apt-get update
sudo apt-get install -y --fix-missing mysql-server
systemctl is-active mysql; systemctl is-enabled mysql
mysql --version

sudo install -m 644 ~/infra/zz-biblioteca.cnf /etc/mysql/mysql.conf.d/zz-biblioteca.cnf
sudo systemctl restart mysql
echo "===== E4: en que direccion escucha MySQL ====="
sudo ss -tlnp | grep mysqld
sudo mysql -e "SHOW VARIABLES WHERE Variable_name IN ('bind_address','innodb_buffer_pool_size','max_connections','performance_schema')"

sudo mysql < ~/sql/01_esquema.sql
sudo mysql biblioteca < ~/sql/02_datos.sql

pedir() {   # $1 = cuenta -> deja la contrasena en $CLAVE
    while true; do
        read -rsp "Contrasena para $1 (minimo 12 caracteres, sin ' ni \\): " CLAVE; echo
        read -rsp "Repitela: " CLAVE2; echo
        if [ "$CLAVE" != "$CLAVE2" ]; then echo "No coinciden."; continue; fi
        if [ "${#CLAVE}" -lt 12 ]; then echo "Demasiado corta."; continue; fi
        case "$CLAVE" in *"'"*|*"\\"*) echo "Contiene un caracter no permitido."; continue;; esac
        break
    done
}
pedir biblioteca_app;   CLAVE_APP="$CLAVE"
pedir biblioteca_admin; CLAVE_ADMIN="$CLAVE"

sudo mysql <<SQL
CREATE USER IF NOT EXISTS 'biblioteca_app'@'localhost'   IDENTIFIED BY '${CLAVE_APP}';
CREATE USER IF NOT EXISTS 'biblioteca_admin'@'localhost' IDENTIFIED BY '${CLAVE_ADMIN}';
GRANT SELECT, INSERT, UPDATE, DELETE ON biblioteca.* TO 'biblioteca_app'@'localhost';
GRANT ALL PRIVILEGES ON biblioteca.* TO 'biblioteca_admin'@'localhost';
FLUSH PRIVILEGES;
SQL
unset CLAVE CLAVE2 CLAVE_APP CLAVE_ADMIN

echo "===== E5: 04_verificacion.sql ====="
sudo mysql biblioteca < ~/sql/04_verificacion.sql
sudo mysql -e "SHOW GRANTS FOR 'biblioteca_app'@'localhost'"
