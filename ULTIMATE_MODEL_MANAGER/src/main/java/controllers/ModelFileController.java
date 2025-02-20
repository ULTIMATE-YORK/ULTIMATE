package controllers;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import project.Project;
import sharedContext.SharedContext;
import utils.FileUtils;

public class ModelFileController {
	
	@FXML private TextArea modelFile;	
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();

	@FXML
	public void initialize() {
		setListeners();
	}
	
	private void setListeners() {
        // Listen for changes to the current model
        project.currentModelProperty().addListener((obs, oldModel, newModel) -> {
            if (newModel != null) {
                try {
                    String fileContent = FileUtils.getFileContent(newModel.getFilePath());
                    Platform.runLater(() -> {
                        modelFile.setText(fileContent);
                    });
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
	}

}
