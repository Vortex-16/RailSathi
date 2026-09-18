#!/usr/bin/env bash
set -e

# Colors for clear terminal output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}====================================================${NC}"
echo -e "${BLUE}        RailSathi All-in-One Runner Script         ${NC}"
echo -e "${BLUE}====================================================${NC}"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

# ----------------------------------------------------
# Step 1: Start Backend Server in the Background
# ----------------------------------------------------
echo -e "\n${YELLOW}[1/3] Preparing and Starting RailSathi Backend Server...${NC}"
cd "$PROJECT_ROOT/backend"

if [ ! -d "node_modules" ]; then
    echo -e "Installing Node.js dependencies (npm install)..."
    npm install --silent
fi

echo -e "Compiling TypeScript backend..."
npm run build

echo -e "Starting backend server on port 8080 in background..."
# Kill any existing server on 8080 if running
fuser -k 8080/tcp 2>/dev/null || true

# Start server redirecting logs to backend.log
export PORT=8080
export NODE_ENV=production
nohup npm start > "$PROJECT_ROOT/backend.log" 2>&1 &
BACKEND_PID=$!
echo -e "${GREEN}✔ Backend server running (PID: $BACKEND_PID) on http://localhost:8080${NC}"
echo -e "  Logs are streaming to: backend.log"

# Wait a brief moment to verify server started
sleep 2
if curl -s http://localhost:8080/health > /dev/null 2>&1; then
    echo -e "${GREEN}✔ Healthcheck passed: backend is responsive!${NC}"
else
    echo -e "${YELLOW}ℹ Backend started, initializing endpoints...${NC}"
fi

# ----------------------------------------------------
# Step 2: Build Android Debug APK
# ----------------------------------------------------
echo -e "\n${YELLOW}[2/3] Compiling Android APK with Gradle...${NC}"
cd "$PROJECT_ROOT"

# Ensure gradlew is executable
chmod +x gradlew

# Build debug APK with memory optimization suitable for Cloud Shell / VM
./gradlew :app:assembleDebug --no-daemon -Dorg.gradle.jvmargs="-Xmx1536m -XX:+UseParallelGC"

# ----------------------------------------------------
# Step 3: Create Shortcut to APK & Display Instructions
# ----------------------------------------------------
echo -e "\n${YELLOW}[3/3] Creating APK Shortcut & Links...${NC}"
APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"

if [ -f "$APK_PATH" ]; then
    # Create a symlink in project root for instant access
    ln -sf "$APK_PATH" "$PROJECT_ROOT/RailSathi-debug.apk"
    
    echo -e "\n${GREEN}====================================================${NC}"
    echo -e "${GREEN}🎉 SUCCESS! RailSathi Build Completed!${NC}"
    echo -e "${GREEN}====================================================${NC}"
    echo -e "1. Backend Server:  ${GREEN}Running on http://localhost:8080${NC}"
    echo -e "   - To preview backend in Cloud Shell: Click 'Web Preview' -> 'Preview on port 8080'"
    echo -e "2. APK Output:      ${GREEN}$PROJECT_ROOT/RailSathi-debug.apk${NC}"
    echo -e "   - Shortcut created at: ${BLUE}RailSathi-debug.apk${NC}"
    echo -e "   - To download to your computer in Cloud Shell:"
    echo -e "     Run: ${YELLOW}cloudshell download $PROJECT_ROOT/RailSathi-debug.apk${NC}"
    echo -e "====================================================\n"
else
    echo -e "${RED}❌ APK was not found at $APK_PATH. Please check Gradle output above.${NC}"
    exit 1
fi
