#!/bin/bash

SCRIPT_DIR="$(dirname "$(realpath "$0")")"
# build the project
mvn clean install
# Check if config.json exists; if not, create it with default content
CONFIG_FILE="$SCRIPT_DIR/config.json"
if [ ! -f "$CONFIG_FILE" ]; then
    echo "{\"stormInstall\" : \"\", \"stormParsInstall\" : \"\" }" > "$CONFIG_FILE"
fi
java --module-path $SCRIPT_DIR/libs/javafx-sdk-23.0.1/lib --add-modules javafx.controls,javafx.fxml -jar $SCRIPT_DIR/target/ultimate_model_manager-0.0.1-SNAPSHOT.jar
