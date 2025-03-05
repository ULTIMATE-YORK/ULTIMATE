package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import parameters.ExternalParameter;
import parameters.UncategorisedParameter;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;
import utils.DialogOpener;

public class AddExternalController {
	
	@FXML private Label valueLabel;
	@FXML private ChoiceBox<UncategorisedParameter> uncategorisedParameters;
	@FXML private ChoiceBox<String> chooseType;
	@FXML private TextField chooseText;
	@FXML private Button chooseButton;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;
	
	private String dataFile = null;
	
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();
	
	@FXML
	private void initialize() {
		uncategorisedParameters.setItems(project.getCurrentModel().getUncategorisedParameters());
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
		dataFile = DialogOpener.openDataFileDialog(sharedContext.getMainStage());
	}
	
	@FXML
	private void saveEParam() {
		UncategorisedParameter name = uncategorisedParameters.getValue();
		String type = chooseType.getValue();
		String value = "";
		if (dataFile == null) {
			value = chooseText.getText();
			// check its a number
		}
		else {
			value = dataFile;
		}
		// FIXME: add check that definition is valid
		if (name == null || type == null|| value == null) {
			Alerter.showErrorAlert("Invalid Parameter", "Please define all parameters!");
			return;
		}
		else {
			ExternalParameter eParam = new ExternalParameter(name.toString(), type, value);
			project.getCurrentModel().addExternalParameter(eParam);
			project.getCurrentModel().removeUncategorisedParameter(uncategorisedParameters.getValue());
			closeDialog();
		}
	}
	
	@FXML
	private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
	}
}
