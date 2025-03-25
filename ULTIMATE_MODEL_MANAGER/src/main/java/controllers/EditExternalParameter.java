package controllers;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import parameters.ExternalParameter;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public class EditExternalParameter {
	
	@FXML private Label valueLabel;
	@FXML private ChoiceBox<String> chooseType;
	@FXML private TextField chooseText;
	@FXML private Button chooseButton;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;
	
	private String dataFile = null;
	
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();
    
    private ExternalParameter ep;
    
	private static final Logger logger = LoggerFactory.getLogger(EditExternalParameter.class);
    
	@FXML
	private void initialize() {
		chooseType.getItems().addAll("Fixed","Mean","Mean-Rate", "Bayes", "Bayes-Rate");
        
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
        });
	}
	
	@FXML
	private void chooseDataFile() {
		dataFile = openDataFileDialog(sharedContext.getMainStage());
	}
	
	@FXML
	private void saveEParam() {
		String type = chooseType.getValue();
		String value = "";
		if (dataFile == null) {
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
		// FIXME: add check that definition is valid
		if (type == null|| value == null) {
			Alerter.showErrorAlert("Invalid Parameter", "Please define all parameters!");
			return;
		}
		else {
			try {
				ExternalParameter eParam = new ExternalParameter(ep.getName(), type, value);
				project.getCurrentModel().removeExternalParameter(ep);
				project.getCurrentModel().addExternalParameter(eParam);
				project.refresh();
				closeDialog();
			} catch (IOException e) {
			}
		}
	}
	
	@FXML
	private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
	}
	
	public void setEP(ExternalParameter ep) {
		this.ep = ep;
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
	        } catch (IOException e) {
	        	logger.error(e.getMessage());
	        	return null;
	        }
	        return selectedFile.getName();
	    }
	    return null; 
	}

}
