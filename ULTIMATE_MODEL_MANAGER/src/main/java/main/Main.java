package main;

import javafx.application.Application; // Importing the base Application class for JavaFX
import javafx.fxml.FXMLLoader; // For loading FXML files
import javafx.scene.Scene; // For creating and managing scenes
import javafx.scene.layout.GridPane; // The layout for the UI root element
import javafx.stage.Stage; // The main stage/window of the JavaFX application

/**
 * Main class for the Ultimate Model Manager JavaFX application.
 * This class sets up and launches the JavaFX application, initializes the main stage,
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
        // Set up the shared context singleton instance for sharing data across the application
        //SharedData sharedContext = SharedData.getInstance(); // Obtain the shared data instance
        //sharedContext.setMainStage(stage); // Store the primary stage in the shared context

        // Load the FXML file and initialize its associated controller
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_view.fxml")); // Specifies the FXML path
        
        // Load the root layout from the FXML file (in this case, a GridPane layout)
        GridPane root = loader.load(); // The root layout is defined in main_view.fxml

        // Create a Scene using the root layout and set its dimensions
        stage.setScene(new Scene(root, 1500, 1000)); // Scene dimensions: 1500x1000 pixels

        // Set the title of the primary stage (window)
        stage.setTitle("Ultimate Stochastic World Model Manager"); // Customize the window title as needed

        // Set minimum dimensions for the primary stage
        stage.setMinWidth(1000); // Ensure the stage cannot be resized smaller than 800px in width
        stage.setMinHeight(800); // Ensure the stage cannot be resized smaller than 600px in height

        // Display the stage (window) to the user
        stage.show(); // Makes the primary stage visible
    }
	
    /**
     * The main method is the starting point of the Java application.
     * It launches the JavaFX application by invoking the `launch` method.
     *
     * @param args Command-line arguments (if any) passed during application startup.
     */
    public static void main(String[] args) {
        launch(args); // Launch the JavaFX application
    }
}