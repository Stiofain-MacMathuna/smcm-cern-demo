#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "--- CERN Telemetry System Smoke Test ---"

# 1. Test Java API
echo -n "Checking Java Telemetry Service (8080)... "
if curl -s --head  --request GET http://localhost:8080/api/v1/telemetry/history | grep "200" > /dev/null
then
    echo -e "${GREEN}PASS${NC}"
else
    echo -e "${RED}FAIL (Service down or DB not ready)${NC}"
fi

# 2. Test Django Status API
echo -n "Checking Django Backend (8000)... "
if curl -s http://localhost:8000/api/get-lhc-status/ | grep -q "status"; then
    echo -e "${GREEN}PASS${NC}"
else
    echo -e "${RED}FAIL (Check Django logs)${NC}"
fi

# 3. Test C++ Data Injection
echo -n "Checking C++ -> Java Persistence Bridge... "
DATA=$(curl -s http://localhost:8080/api/v1/telemetry/history)
if [[ $DATA == *"sensorId"* ]]; then
    echo -e "${GREEN}PASS (Data is flowing!)${NC}"
else
    echo -e "${RED}FAIL (C++ sensor not pushing or Java not saving)${NC}"
fi

echo "---------------------------------------"