#!/bin/bash

# Token Ring 6 Servers - Start Script
# Reads environment variables and starts the server
# Uses pre-compiled .class files from build/ folder

# Get environment variables with defaults
NODE_ID=${NODE_ID:-1}
PORT=${PORT:-8080}
MYSQL_URL=${MYSQL_URL:-"jdbc:mysql://localhost:3306/db1"}
PEERS=${PEERS:-""}

echo "=========================================="
echo "Token Ring Server Startup"
echo "=========================================="
echo "[INFO] NODE_ID: $NODE_ID"
echo "[INFO] PORT: $PORT"
echo "[INFO] MYSQL_URL: $MYSQL_URL"
echo "[INFO] PEERS: ${PEERS:-'(will be set by PM later)'}"
echo "=========================================="

# Download MySQL driver if not present
if [ ! -f "lib/mysql-connector-java-8.0.33.jar" ]; then
    echo "[DOWNLOAD] Downloading MySQL JDBC driver..."
    mkdir -p lib
    cd lib
    if ! wget -q https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar; then
        echo "[ERROR] Failed to download MySQL driver"
        exit 1
    fi
    cd ..
    echo "[DOWNLOAD] MySQL driver downloaded successfully"
else
    echo "[DOWNLOAD] MySQL driver already exists"
fi

# Verify pre-compiled classes exist
if [ ! -d "build/server" ]; then
    echo "[ERROR] Pre-compiled classes not found in build/server"
    echo "[ERROR] Please run: javac -d build src/server/*.java src/client/*.java"
    exit 1
fi

echo "[START] Starting server with pre-compiled classes..."
export NODE_ID PORT MYSQL_URL PEERS
java -cp "build:lib/mysql-connector-java-8.0.33.jar" server.Main
