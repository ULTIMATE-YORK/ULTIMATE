package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;
import utils.FileUtils;

public class ConfigureStormController {
	
	@FXML private TextField stormField;
	@FXML private TextField stormParsField;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;
	
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();	
	
	@FXML
	private void save() {
		String si = stormField.getText();
		String spi = stormParsField.getText();
		if (si.equals("") || spi.equals("")) {
			Alerter.showErrorAlert("Invalid Path", "The text is not a file path!");
			return;
		}
		if (!FileUtils.isFile(si) || !FileUtils.isFile(spi)) {
			Alerter.showErrorAlert("Invalid Path", "The path provided is not a valid path!");
			return;
		}
		project.setStormInstall(si);
		project.setStormParsInstall(spi);
		cancel();
	}
	
	@FXML
	private void cancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
	}

}
