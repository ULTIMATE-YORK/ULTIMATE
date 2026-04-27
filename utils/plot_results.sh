#!/bin/bash
# Wrapper for plot_results.py
#
# Usage (from any directory):
#   plot_results.sh FILE.dat
#   plot_results.sh FILE1.dat FILE2.dat
#
# The output PDF is placed next to each input .dat file.
#
# The script reuses the Python virtual environment that launch_ultimate.sh
# creates at the repository root (.venv).  If it does not exist yet it is
# created automatically — this also works on Ubuntu.

set -euo pipefail

UTILS_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$UTILS_DIR/.." && pwd)"
VENV_DIR="$REPO_ROOT/.venv"
REQ_FILE="$REPO_ROOT/requirements.txt"

# Create the venv and install dependencies if it does not exist yet.
if [ ! -d "$VENV_DIR" ]; then
    echo "Creating Python virtual environment at $VENV_DIR ..."
    python3 -m venv "$VENV_DIR"
    echo "Installing Python dependencies from $REQ_FILE ..."
    "$VENV_DIR/bin/pip" install --quiet -r "$REQ_FILE"
    echo "Done."
fi

exec "$VENV_DIR/bin/python3" "$UTILS_DIR/plot_results.py" "$@"
