package main;

import javafx.application.Application; // Importing the base Application class for JavaFX
import javafx.fxml.FXMLLoader; // For loading FXML files
import javafx.scene.Scene; // For creating and managing scenes
import javafx.scene.layout.GridPane; // The layout for the UI root element
import javafx.stage.Stage; // The main stage/window of the JavaFX application
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

/**
 * Main class for the Ultimate Model Manager JavaFX application.
 * This class sets up and launches the JavaFX application, initializes the main
 * stage,
 * and loads the primary FXML view.
 */
public class Main extends Application {

    /**
     * The entry point for the JavaFX application lifecycle.
     * Initializes the primary stage and sets up the scene.
     *
     * @param stage The primary stage for this application, provided by JavaFX.
     * @throws Exception If loading the FXML file or other initialization fails.
     */
    @Override
    public void start(Stage stage) throws Exception {
        // Set up the shared context singleton instance for sharing data across the
        // application

        SharedContext.setMainStage(stage);
        SharedContext.reset(new Project());

    }

    /**
     * The main method is the starting point of the Java application.
     * It launches the JavaFX application by invoking the `launch` method.
     *
     * @param args Command-line arguments (if any) passed during application
     *             startup.
     */
    public static void main(String[] args) {
        launch(args); // Launch the JavaFX application
    }
}