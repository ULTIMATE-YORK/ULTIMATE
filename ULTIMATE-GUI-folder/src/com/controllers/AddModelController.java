package com.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.parameters.Model;
import com.parameters.UndefinedParameter;
import com.ultimate.modelmanager.utils.PrismFileParser;

/**
 * Controller class for the "Add Model" dialog in the Ultimate Model Manager application.
 * <p>
 * This controller allows the user to add a new model to the system by providing a model ID
 * and selecting a file. The file is parsed for undefined parameters, which are added to the model.
 * </p>
 */
public class AddModelController {

    // UI elements mapped from FXML
    @FXML
    private TextField idField; // Input field for model ID
    @FXML
    private TextField filePathField; // Input field for the model file path
    @FXML
    private Button addModelSaveButton; // Button to save the model

    // References to application stages and model list
    private Stage mainStage; // The main application window
    private Stage editorStage; // The dialog window used to add or edit models
    private List<Model> models; // The list of existing models managed by the application

    /**
     * Constructor for initializing the AddModelController with necessary dependencies.
     *
     * @param mainStage   the main stage of the application
     * @param editorStage the stage used to display the "Add Model" dialog
     * @param models      the list of models to which the new model will be added
     */
    public AddModelController(Stage mainStage, Stage editorStage, List<Model> models) {
        this.mainStage = mainStage;
        this.editorStage = editorStage;
        this.models = models;
    }

    /**
     * Initializes the controller by setting up event handlers for UI elements.
     * This method is called after the FXML fields are injected.
     */
    @FXML
    public void initialize() {
        // Sets the action to save the model when the save button is clicked
        addModelSaveButton.setOnAction(e -> saveModel());

        // Opens a file dialog for the user to select a model file
        openFileDialog();
    }

    /**
     * Opens a file chooser dialog to allow the user to select a model file.
     * Once a file is selected, it updates the file path and model ID fields accordingly.
     */
    private void openFileDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Model File");
        // List of valid file types
        String[] validFileTypes = {"*.ctmc", "*.dtmc", "*.pomdp", "*.prism"};
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Prism Files", validFileTypes));

        // Show file chooser dialog and capture the selected file
        File selectedFile = fileChooser.showOpenDialog(mainStage);
        if (selectedFile != null) {
            // Update the text fields with the selected file's path and inferred model ID
            filePathField.setText(selectedFile.getAbsolutePath());
            idField.setText(selectedFile.getName().replaceFirst("[.][^.]+$", "")); // Remove file extension to set as model ID
        }
    }

    /**
     * Saves the model by validating the input and adding it to the list of models.
     * Also parses the model file for undefined parameters and associates them with the model.
     * <p>
     * If either the ID or file path is empty, an alert is shown to the user.
     * </p>
     */
    private void saveModel() {
        String id = idField.getText().trim();
        String filePath = filePathField.getText().trim();

        // Validate that both the model ID and file path are provided
        if (id.isEmpty() || filePath.isEmpty()) {
            showAlert("Validation Error", "Both ID and file path are required.");
            return;
        }

        // Create a new model and add it to the list of models
        Model newModel = new Model(id, filePath);
        models.add(newModel);

        // Parse the file for undefined parameters using PrismFileParser
        PrismFileParser parser = new PrismFileParser();
        try {
            List<String> undefinedParams = parser.parseFile(filePath);

            if (undefinedParams != null) {
                // For each undefined parameter, create an UndefinedParameter object
                for (String param : undefinedParams) {
                    String[] parts = param.split("\\s+");
                    if (parts.length == 3) {
                        String paramName = parts[2];
                        UndefinedParameter up = new UndefinedParameter(paramName);
                        newModel.addUndefinedParameter(up.getName());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Log parsing errors for debugging purposes
        }

        // Close the editor stage after saving the model
        editorStage.close();
    }

    /**
     * Displays an alert to the user with the specified title and message.
     *
     * @param title   the title of the alert
     * @param message the message to display in the alert
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}