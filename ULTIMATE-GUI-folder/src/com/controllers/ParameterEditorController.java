package com.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

import com.parameters.*;

public class ParameterEditorController {

    private Stage editorStage;
    private Model currentModel;
    private Parameter parameter;
    private boolean isEditMode;
    private String type;
    private String[] modelIDs;

    @FXML private TextField nameField; // Common field for all parameters
    @FXML private TextField customField1; // Specific to certain Parameter types
    @FXML private TextField customField2; // Specific to certain Parameter types
    @FXML private ChoiceBox<UndefinedParameter> undefinedParameters; // Optional for drop-downs
    @FXML private ChoiceBox<String> calculations; // Optional for drop-downs
    @FXML private ChoiceBox<String> chooseModelID;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button chooseDataFile; // Specific to EnvironmentParameters

    /**
     * Constructor for the ParameterEditorController.
     *
     * @param editorStage   The stage for the editor dialog
     * @param currentModel  The model associated with the parameter
     * @param parameter     The parameter to edit (null if adding a new parameter)
     * @param type          The type of parameter (e.g. "Environment", "Dependency")
     * @param modelIDs      The IDs of the models to choose from (for DependancyParameters)
     */
    public ParameterEditorController(Stage editorStage, Model currentModel, Parameter parameter, String type, String[] modelIDs) {
        this.editorStage = editorStage;
        this.currentModel = currentModel;
        this.parameter = parameter;
        this.isEditMode = parameter != null;
        this.type = type;
        this.modelIDs = modelIDs;
    }

    @FXML
    private void initialize() {
        // Initialise fields if editing an existing parameter
        if (isEditMode) {
            populateFields(parameter);
        }
        
		// set the choice box options to the current model's undefined parameters
		ObservableList<UndefinedParameter> observableItems = FXCollections.observableArrayList(currentModel.getUndefinedParameters());
        undefinedParameters.setItems(observableItems);  // Set the list as items for the ChoiceBox
        undefinedParameters.setOnAction(e -> getName());
        
        // Set up button actions
        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
        
        try {
	        // Set up the calculations choice box
	    	// TODO define the calculations for each model in another file
	        calculations.setItems(FXCollections.observableArrayList("Rate"));
	        // Set the custom field 2 text to the selected item in the calculations
		    calculations.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
		            customField2.setText(newValue);
		    });
        	// Set up the model ID choice box
	        chooseDataFile.setOnAction(e -> handleChooseDataFile());
        }
		catch (NullPointerException x) {
			// Handle the exception
		}
        
        try {
	        chooseModelID.setItems(FXCollections.observableArrayList(modelIDs));
			chooseModelID.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
				customField1.setText(newValue);
			});
        }
		catch (NullPointerException x) {
			// Handle the exception
		}
    }
 
	private void getName() {
		nameField.setText(undefinedParameters.getValue().getName());
	}
	
	private void handleChooseDataFile() {
		// Open a file chooser dialog and set the file path
		// This method is specific to EnvironmentParameters
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose data file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Data Files", "*.dat"));
        File file = fileChooser.showOpenDialog(editorStage);
		if (file != null) {
			customField1.setText(file.getAbsolutePath());
		}
	}

    /**
     * Populates the fields with the parameter's data (for edit mode).
     *
     * @param parameter The parameter to edit.
     */
    private void populateFields(Parameter parameter) {
        nameField.setText(parameter.getName());
        // Set the undefinedParameters to the value of nameField and disable it
        undefinedParameters.setValue(new UndefinedParameter(nameField.getText()));
        undefinedParameters.setDisable(true);
        // Populate specific fields based on the parameter type
        if (parameter instanceof EnvironmentParameter) {
            EnvironmentParameter envParam = (EnvironmentParameter) parameter;
            customField1.setText(envParam.getFilePath());
            customField2.setText(envParam.getCalculation());
        } else if (parameter instanceof DependancyParameter) {
            DependancyParameter depParam = (DependancyParameter) parameter;
            customField1.setText(depParam.getModelID());
            customField2.setText(depParam.getDefinition());
        }
    }

    /**
     * Handles the save action.
     */
    private void handleSave() {
        // Collect the data from the fields
        String name = nameField.getText();
        
        // These data may exist so try retrieving them
        try {
			String field1 = customField1.getText();
			String field2 = customField2.getText();
		} catch (NullPointerException e) {
			// Handle the exception
        }

        // Validate input
        if (name.isEmpty()) {
            showError("Name is required!");
            return;
        }

        // Update existing parameter or create a new one
        if (isEditMode) {
            updateParameter(parameter, name);
        } else {
            Parameter newParameter = createParameter(name);
            addParameterToModel(newParameter);
        }
		currentModel.removeUndefinedParamter(undefinedParameters.getValue());
        editorStage.close(); // Close the editor dialog
    }

    /**
     * Updates the parameter with data from the fields.
     *
     * @param parameter The parameter to update.
     * @param name      The name of the parameter.
     */
    private void updateParameter(Parameter parameter, String name) {
        parameter.setName(name);

        if (parameter instanceof EnvironmentParameter) {
            EnvironmentParameter envParam = (EnvironmentParameter) parameter;
            envParam.setFilePath(customField1.getText());
            envParam.setCalculation(customField2.getText());
        } else if (parameter instanceof DependancyParameter) {
            DependancyParameter depParam = (DependancyParameter) parameter;
            depParam.setModelID(customField1.getText());
            depParam.setDefinition(customField2.getText());
        }
    }

    /**
     * Creates a new parameter based on the type.
     *
     * @param name The name of the parameter.
     * @return The newly created parameter.
     */
    private Parameter createParameter(String name) {
        // Determine the parameter type and create the appropriate instance
        if (type.equals("Environment")) {
            return new EnvironmentParameter(name, customField1.getText(), customField2.getText());
        } else if (type.equals("Dependency")) {
            return new DependancyParameter(name, customField1.getText(), customField2.getText());
        } else {
            return new InternalParameter(name);
        }
    }

    /**
     * Adds a new parameter to the model.
     *
     * @param parameter The parameter to add.
     */
    private void addParameterToModel(Parameter parameter) {
        if (parameter instanceof EnvironmentParameter) {
            currentModel.addEnvironmentParameter((EnvironmentParameter) parameter);
        }
		else if (parameter instanceof DependancyParameter) {
			currentModel.addDependencyParameter((DependancyParameter) parameter);
		} else {
			currentModel.addInternalParameter((InternalParameter) parameter);
		}
    }

    /**
     * Handles the cancel action.
     */
    private void handleCancel() {
        editorStage.close(); // Close the editor dialog without saving
    }

    /**
     * Shows an error message.
     *
     * @param message The error message to display.
     */
    private void showError(String message) {
        // Show error message (use an Alert or a Label in the UI)
        System.err.println("Error: " + message);
    }
}