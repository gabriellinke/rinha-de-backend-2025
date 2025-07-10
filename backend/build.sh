#!/bin/bash

IMAGE_NAME="gabriellinke/backend:latest"

echo "🔨 Buildando a imagem Docker: $IMAGE_NAME"
docker build -t $IMAGE_NAME .

echo "✅ Build finalizado com sucesso."
