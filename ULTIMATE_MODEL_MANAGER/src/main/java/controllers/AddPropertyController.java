package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public class AddPropertyController {
	
	@FXML private TextField propertyDialogDefinition;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;
	
    private Project project = SharedContext.getProject();
    
	@FXML
	private void save() {
		String property = propertyDialogDefinition.getText();
		if (property.equals("")) {
			Alerter.showErrorAlert("Please define the property to save", "");
			return;
		}
		else {
			project.getTargetModel().addProperty(property);
			cancel();
		}
	}
	
	@FXML
	private void cancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
	}

}
