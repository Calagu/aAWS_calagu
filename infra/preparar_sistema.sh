#!/usr/bin/env bash
# FASE 2 (en la instancia): zona horaria, hostname, actualizacion y swap de 1 GiB.  -> Evidencia E3
set -euo pipefail
export DEBIAN_FRONTEND=noninteractive NEEDRESTART_MODE=a

sudo timedatectl set-timezone Europe/Madrid
sudo hostnamectl set-hostname ad-calagu
sudo apt-get update
sudo apt-get upgrade -y

if ! swapon --show | grep -q /swapfile; then
    sudo fallocate -l 1G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    grep -q '^/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
fi

echo "===== E3: timedatectl y free -h ====="
timedatectl
free -h
