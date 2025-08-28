package controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.json.JSONObject;

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
	
    private Project project = SharedContext.getProject();
	
	@FXML
	private void save() throws IOException {
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
		// reflect this in the config file
		JSONObject root = new JSONObject();
		root.put("stormInstall", si);
		root.put("stormParsInstall", spi);
		String content = root.toString();
		Files.write(Paths.get(System.getenv("ULTIMATE_DIR") + "/config.json"), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		cancel();
	}
	
	@FXML
	private void cancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
	}

}
