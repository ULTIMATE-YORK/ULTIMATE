package controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import parameters.ExternalParameter;
import parameters.UncategorisedParameter;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public class AddExternalController {
	
	@FXML private Label valueLabel;
	@FXML private ChoiceBox<UncategorisedParameter> uncategorisedParameters;
	@FXML private ChoiceBox<String> chooseType;
	@FXML private Label rangeLabel;
	@FXML private TextField rangeText;
	@FXML private TextField chooseText;
	@FXML private Button chooseButton;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;
	
	private String dataFile = null;
	
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();
	private static final Logger logger = LoggerFactory.getLogger(AddExternalController.class);
	
	private boolean ranged = false;
	ArrayList<Double> rangeValues = new ArrayList<Double>();
	
	@FXML
	private void initialize() {
		uncategorisedParameters.setItems(project.getCurrentModel().getUncategorisedParameters());
		chooseType.getItems().addAll("Fixed","Mean","Mean-Rate", "Bayes", "Bayes-Rate", "Ranged");
        
		valueLabel.setManaged(false);
		valueLabel.setVisible(false);
		chooseText.setVisible(false);
        chooseText.setManaged(false); // Ensures it doesn't take up space when hidden
        chooseButton.setVisible(false);
        chooseButton.setManaged(false);
        
        // Add listener to handle visibility
        chooseType.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
        	if (newVal.equals("Fixed")) {
        		// show text area and hide button
        		chooseText.setVisible(true);
        		chooseText.setManaged(true);
        		chooseButton.setVisible(false);
        		chooseButton.setManaged(false);
        		valueLabel.setManaged(true);
        		valueLabel.setVisible(true);
        	}
        	else {
        		// show button
        		chooseText.setVisible(false);
        		chooseText.setManaged(false);
        		chooseButton.setVisible(true);
        		chooseButton.setManaged(true);
        		valueLabel.setManaged(true);
        		valueLabel.setVisible(true);
        	}
        	
			if (newVal.equals("Ranged")) {
				ranged = true;
				rangeLabel.setVisible(true);
				rangeLabel.setManaged(true);
				rangeText.setVisible(true);
				rangeText.setManaged(true);
				chooseButton.setVisible(false);
				chooseButton.setManaged(false);
				valueLabel.setVisible(false);
				valueLabel.setManaged(false);
				chooseText.setVisible(false);
				chooseText.setManaged(false);
			}
        
        });
	}
	
	@FXML
	private void chooseDataFile() {
		dataFile = openDataFileDialog(sharedContext.getMainStage());
	}
	
	@FXML
	private void saveEParam() {
		UncategorisedParameter name = uncategorisedParameters.getValue();
		String type = chooseType.getValue();
		String value = "";
		if (dataFile == null && !ranged) {
			value = chooseText.getText();
			// check its a number
			try {
			    double doubleValue = Double.parseDouble(value);
			} catch (NumberFormatException e) {
	            Alerter.showErrorAlert("Invalid Value", "The value must be in the range 0.0 <= x <= 1.0");
	            return;
			}
		}
		else {
			value = dataFile;
		}
		

if (ranged) {
    value = rangeText.getText();
    try {
        String[] parts = value.split(","); // Split the input by commas
        for (String part : parts) {
            rangeValues.add(Double.parseDouble(part.trim())); // Parse and add to the list
        }
    } catch (NumberFormatException e) {
        Alerter.showErrorAlert("Invalid Range", "Please enter a valid range of numbers separated by commas.");
        return;
    }
    // Use rangeValues as needed
}

		
		// FIXME: add check that definition is valid
		if (name == null || type == null|| value == null) {
			Alerter.showErrorAlert("Invalid Parameter", "Please define all parameters!");
			return;
		}
		else {
			if (!ranged) {
				try {
					ExternalParameter eParam = new ExternalParameter(name.toString(), type, value);
					project.getCurrentModel().addExternalParameter(eParam);
					project.getCurrentModel().removeUncategorisedParameter(uncategorisedParameters.getValue());
					closeDialog();
				} catch (IOException e) {
					closeDialog();
				} catch (NumberFormatException e) {
					Platform.runLater(() -> Alerter.showErrorAlert("Invalid File type", e.getMessage()));
					closeDialog();            }
			}
			else {
				try {
					ExternalParameter eParam = new ExternalParameter(name.toString(), type, rangeValues);
					project.getCurrentModel().addExternalParameter(eParam);
					project.getCurrentModel().removeUncategorisedParameter(uncategorisedParameters.getValue());
					closeDialog();
				} catch (IOException e) {
					closeDialog();
				} catch (NumberFormatException e) {
					Platform.runLater(() -> Alerter.showErrorAlert("Invalid File type", e.getMessage()));
					closeDialog();            }
			}

		}
	}
	
	@FXML
	private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
	}
	
	private String openDataFileDialog(Stage stage) {
	    FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle("Choose a Data File");
	    // Set the initial directory (change the path to your specific directory)
	    File initialDir = null;
	    try {
	    	 initialDir = new File(project.directory());
	    } catch (Exception e) {
	    	
	    }
	    if(initialDir != null){
	        fileChooser.setInitialDirectory(initialDir);
	    }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Data files (*.dat, *txt)", "*.dat", "*.txt"));
	    File selectedFile = fileChooser.showOpenDialog(stage);
	    // Check if the file is in the desired directory
	    if (selectedFile != null) {
	        try {
	            // Get the canonical paths to compare accurately (handles symbolic links, etc.)
	            String selectedPath = selectedFile.getCanonicalPath();
	            String allowedDirPath = initialDir.getCanonicalPath();

	            if (!selectedPath.startsWith(allowedDirPath)) {
	                // The file is not in the allowed directory
	            	Alerter.showErrorAlert("Data File must be in project directory!", "Choose a file from the same directory as the project");
	                return null;
	            }
	        } catch (Exception e) {
	        	logger.error(e.getMessage());
	        	return null;
	        }
	        return selectedFile.getName();
	    }
	    return null; 
	}
}
