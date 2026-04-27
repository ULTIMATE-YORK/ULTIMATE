#!/bin/bash
# ULTIMATE Launcher

echo "Starting ULTIMATE - Stochastic World Model Verification & Synthesis..."

# Resolve the directory containing this script
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
export ULTIMATE_DIR="$SCRIPT_DIR/ULTIMATE_MODEL_MANAGER"

# Pin to Java 21 on macOS: Java 24+ uses stricter code signing that causes macOS
# to strip DYLD_LIBRARY_PATH, which EvoChecker requires for synthesis.
if [[ "$OSTYPE" == "darwin"* ]]; then
    JAVA_21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null)
    if [[ -n "$JAVA_21_HOME" ]]; then
        export JAVA_HOME="$JAVA_21_HOME"
    fi
fi

# Set native library path (macOS uses DYLD_LIBRARY_PATH, Linux uses LD_LIBRARY_PATH)
if [[ "$OSTYPE" == "darwin"* ]]; then
    # On Apple Silicon, the ARM64 dylibs live in libs/runtime-amd64 (despite the name).
    # macOS Sequoia strips DYLD_LIBRARY_PATH from hardened JVM processes at exec time,
    # so we also set JAVA_TOOL_OPTIONS: unlike DYLD_*, it is not stripped and is read
    # by every JVM in the chain (Maven and EvoChecker subprocesses alike).
    if [[ "$(uname -m)" == "arm64" ]]; then
        LIBS_DIR="$ULTIMATE_DIR/libs/runtime-amd64"
    else
        LIBS_DIR="$ULTIMATE_DIR/libs/runtime"
    fi
    export DYLD_LIBRARY_PATH="$LIBS_DIR:$DYLD_LIBRARY_PATH"
    DEFAULT_JLP="$HOME/Library/Java/Extensions:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:."
    export JAVA_TOOL_OPTIONS="-Djava.library.path=$LIBS_DIR:$DEFAULT_JLP"
else
    export LD_LIBRARY_PATH="$ULTIMATE_DIR/libs/runtime:$LD_LIBRARY_PATH"
fi

# Suppress the jansi "restricted method" JVM warning.
# --add-opens java.base/java.util=ALL-UNNAMED is needed so that ULTIMATE can inject
# DYLD_LIBRARY_PATH into the JVM environment at runtime: macOS 15 (Sequoia) strips
# DYLD_* variables from hardened JVM processes even when the JDK has the
# allow-dyld-environment-variables entitlement.
export MAVEN_OPTS="--enable-native-access=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED"

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
  "terminally deprecated|sun\.misc\.Unsafe|Please consider reporting|Unsupported JavaFX configuration|\+\[IMKClient|Picked up JAVA_TOOL_OPTIONS" \
  >&2)

echo "ULTIMATE closed."
