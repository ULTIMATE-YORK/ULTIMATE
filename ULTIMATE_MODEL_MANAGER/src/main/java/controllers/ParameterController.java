package controllers;

import javafx.application.Platform;
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
        // When a new model is selected, update the ListView with its uncategorised parameters
        project.currentModelProperty().addListener((obs, oldModel, newModel) -> {
            if (newModel != null) {
                // Retrieve the list of Uncategorised Parameters from the new model.
                // Assuming getUCPARAMS() returns an ArrayList<UncategorisedParameter>
                // You can directly set the items if you convert the list to an observable list:
                Platform.runLater(() -> {
                    uParamList.setItems(javafx.collections.FXCollections.observableArrayList(newModel.getUncategorisedParameters()));
                });
            } else {
                // Clear the ListView if no current model is selected
                Platform.runLater(() -> uParamList.getItems().clear());
            }
        });
		
    	// Customise the appearance of each item in the list view
        uParamList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(UncategorisedParameter item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label label = new Label(item.getName()); // Display the model ID
                    label.setStyle(Font.UC_LIST_FONT); // Apply font styling
                    setGraphic(label); // Set the label as the cell's graphic
                    setText(null); // Clear any text (not needed with graphic)
                }
            }
        });
	}

}
