#!/bin/bash
# ULTIMATE Headless Launcher
#
# Usage (from the repo root):
#   ./launch_ultimate_headless.sh -pf <project.ultimate> [-o <output_dir>]
#
# All arguments are forwarded to ultimate-headless.jar unchanged.

echo "Starting ULTIMATE headless mode..."

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
export ULTIMATE_DIR="$SCRIPT_DIR/ULTIMATE_MODEL_MANAGER"

# Pin to Java 21 on macOS (Java 24+ breaks EvoChecker synthesis via DYLD stripping).
if [[ "$OSTYPE" == "darwin"* ]]; then
    JAVA_21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null)
    if [[ -n "$JAVA_21_HOME" ]]; then
        export JAVA_HOME="$JAVA_21_HOME"
    fi
fi

# Set native library path (macOS: DYLD_LIBRARY_PATH; Linux: LD_LIBRARY_PATH).
if [[ "$OSTYPE" == "darwin"* ]]; then
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
export MAVEN_OPTS="--enable-native-access=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED"

# Create Python virtual environment if it does not exist (needed for synthesis).
VENV_DIR="$SCRIPT_DIR/.venv"
if [ ! -d "$VENV_DIR" ]; then
    echo "Creating Python virtual environment..."
    python3 -m venv "$VENV_DIR"
    echo "Installing Python dependencies..."
    "$VENV_DIR/bin/pip" install -r "$SCRIPT_DIR/requirements.txt" -q
fi
source "$VENV_DIR/bin/activate"

JAR="$ULTIMATE_DIR/target/ultimate-headless.jar"
if [ ! -f "$JAR" ]; then
    echo "Headless jar not found at $JAR — building..."
    cd "$ULTIMATE_DIR"
    mvn package -q
fi

# Resolve any relative file-path arguments to absolute paths before we cd,
# so that -pf and -o arguments given relative to the call site still work.
ARGS=()
for arg in "$@"; do
    if [[ "$arg" != -* ]] && [ -e "$arg" ]; then
        ARGS+=("$(cd "$(dirname "$arg")" && pwd)/$(basename "$arg")")
    else
        ARGS+=("$arg")
    fi
done

# Run the jar from ULTIMATE_MODEL_MANAGER so that relative paths in
# evochecker_config.properties (e.g. libs/runtime-amd64) resolve correctly.
cd "$ULTIMATE_DIR"
exec java \
    --enable-native-access=ALL-UNNAMED \
    --add-opens java.base/java.util=ALL-UNNAMED \
    -jar "$JAR" "${ARGS[@]}"
