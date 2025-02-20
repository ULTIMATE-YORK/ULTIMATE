package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import parameters.UncategorisedParameter;
import project.Project;
import sharedContext.SharedContext;
import utils.Font;

public class ParameterController {
	
	@FXML private ListView<UncategorisedParameter> uParamList;
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();

	@FXML
	public void initialize() {
		setListeners();
	}
	
	private void setListeners() {	
		// When a new model is selected, the list of Uncategorised params is updated
    	project.currentModelProperty().addListener((obs, oldModel, newModel) -> {
    		System.out.println("Updating list of Uncategorised Parameters");
    	});
    	
	}

}
