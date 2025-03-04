package controllers;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;
import utils.FileUtils;

public class ModelFileController {
	
	@FXML private TextArea modelFile;	
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();

	@FXML
	public void initialize() {
		setListeners();
		firstcall();
		
	}
	
	private void firstcall() {
		CompletableFuture.supplyAsync( () -> {
            try {
                return FileUtils.getFileContent(project.getCurrentModel().getFilePath());
            } catch (IOException e) {
            	Alerter.showErrorAlert("Model File not found!", e.getMessage());
            	return null;
            }
		}).thenAccept(fileContent -> 
        	Platform.runLater(() -> modelFile.setText(fileContent))
		);
	}
	
	private void setListeners() {
	    // Listen for changes to the current model
	    project.currentModelProperty().addListener((obs, oldModel, newModel) -> {
	        if (newModel != null) {
	            CompletableFuture.supplyAsync(() -> {
	                try {
	                    return FileUtils.getFileContent(newModel.getFilePath());
	                } catch (IOException e) {
	                	Alerter.showErrorAlert("Model File not found!", e.getMessage());
	                	return null;
	                }
	            }).thenAccept(fileContent -> 
	                Platform.runLater(() -> modelFile.setText(fileContent))
	            );
	        }
	    });
	}

}
