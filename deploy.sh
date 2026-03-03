#!/bin/bash
docker compose up --build -d

echo "Waiting for services to pass health checks..."
sleep 30 

./smoke_test.sh
