#!/bin/bash
# ULTIMATE Launcher

echo "Starting ULTIMATE - Stochastic World Model Verification & Synthesis..."

# Resolve the directory containing this script
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
export ULTIMATE_DIR="$SCRIPT_DIR/ULTIMATE_MODEL_MANAGER"

# Set native library path (macOS uses DYLD_LIBRARY_PATH, Linux uses LD_LIBRARY_PATH)
if [[ "$OSTYPE" == "darwin"* ]]; then
    export DYLD_LIBRARY_PATH="$ULTIMATE_DIR/libs/runtime:$DYLD_LIBRARY_PATH"
else
    export LD_LIBRARY_PATH="$ULTIMATE_DIR/libs/runtime:$LD_LIBRARY_PATH"
fi

# Suppress the jansi "restricted method" JVM warning
export MAVEN_OPTS="--enable-native-access=ALL-UNNAMED"

# Create Python virtual environment if it does not exist, then activate it
VENV_DIR="$SCRIPT_DIR/.venv"
if [ ! -d "$VENV_DIR" ]; then
    echo "Creating Python virtual environment..."
    python3 -m venv "$VENV_DIR"
    echo "Installing Python dependencies..."
    "$VENV_DIR/bin/pip" install -r "$SCRIPT_DIR/requirements.txt" -q
fi
source "$VENV_DIR/bin/activate"

# Launch with Maven (GUI mode), filtering remaining benign third-party warnings
cd "$ULTIMATE_DIR"
mvn exec:java 2> >(grep -Ev \
  "terminally deprecated|sun\.misc\.Unsafe|Please consider reporting|Unsupported JavaFX configuration|\+\[IMKClient" \
  >&2)

echo "ULTIMATE closed."
