#!/bin/bash

SCRIPT_DIR="$(dirname "$(realpath "$0")")"
# build the project
mvn clean install
echo "{}" >> config.json
java --module-path $SCRIPT_DIR/libs/javafx-sdk-23.0.1/lib --add-modules javafx.controls,javafx.fxml -jar $SCRIPT_DIR/target/ultimate_model_manager-0.0.1-SNAPSHOT.jar