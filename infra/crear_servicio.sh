#!/usr/bin/env bash
# FASE 6 (en la instancia): Java, usuario del servicio, fichero de entorno (600) y unidad systemd.
# Requiere /tmp/biblioteca.jar y ~/infra/biblioteca.service. La contrasena de biblioteca_app se pide por teclado.
set -euo pipefail

sudo apt-get update
sudo apt-get install -y --fix-missing openjdk-21-jre-headless
java -version
id biblioteca >/dev/null 2>&1 || sudo useradd --system --no-create-home --shell /usr/sbin/nologin biblioteca
sudo install -d -o biblioteca -g biblioteca -m 755 /opt/biblioteca
sudo install -d -m 755 /etc/biblioteca

read -rsp "Contrasena de biblioteca_app (la misma de MySQL): " CLAVE; echo
( umask 077
  sudo tee /etc/biblioteca/biblioteca.env >/dev/null <<ENV
JAVA_OPTS=-Xms128m -Xmx384m
BIBLIOTECA_DB_USER=biblioteca_app
BIBLIOTECA_DB_PASSWORD=${CLAVE}
BIBLIOTECA_ALUMNO=calagu
BIBLIOTECA_PRESTAMO_MAXIMO=3
BIBLIOTECA_PRESTAMO_DIAS=14
ENV
)
unset CLAVE
sudo chown root:root /etc/biblioteca/biblioteca.env
sudo chmod 600 /etc/biblioteca/biblioteca.env

sudo install -m 644 ~/infra/biblioteca.service /etc/systemd/system/biblioteca.service
sudo install -o biblioteca -g biblioteca -m 644 /tmp/biblioteca.jar /opt/biblioteca/biblioteca.jar
sudo systemctl daemon-reload
sudo systemctl enable --now biblioteca
sleep 15
echo "===== E9: systemctl status ====="
systemctl status biblioteca --no-pager
echo "===== comprobacion local ====="
curl -i http://127.0.0.1:8080/api/estado
