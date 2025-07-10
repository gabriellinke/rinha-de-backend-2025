#!/bin/bash

set -e

# Caminho relativo para o docker-compose dos payment processors
PAYMENT_PROCESSOR_DIR="../payment-processor"

echo "🧹 Parando containers anteriores..."
docker-compose down

echo "🧼 Parando payment-processors anteriores (se houver)..."
docker-compose -f "$PAYMENT_PROCESSOR_DIR/docker-compose.yml" down

echo "🔌 Subindo payment-processors..."
docker-compose -f "$PAYMENT_PROCESSOR_DIR/docker-compose.yml" up -d

echo "⏳ Aguardando alguns segundos para garantir que os processors estejam prontos..."
sleep 3

echo "🚀 Iniciando backend com load balancer..."
docker-compose up -d

echo "📡 Containers em execução:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo "📜 Logs do Nginx (load balancer)..."
docker-compose logs -f nginx
