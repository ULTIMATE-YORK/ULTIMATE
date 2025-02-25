package controllers;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import project.Project;
import property.Property;
import sharedContext.SharedContext;
import utils.DialogOpener;
import utils.Font;

public class PropertiesController {
	
	@FXML private Button addProperty;
	@FXML private Button scrollUp;
	@FXML private Button scrollDown;
	@FXML private ListView<Property> propertyListView;
	
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();
	
	@FXML
	private void initialize() {
		if (project.getCurrentModel() != null) {
				propertyListView.setItems(project.getCurrentModel().getProperties());
		}
		setCells();
		setListeners();
	}
	
	@FXML
	private void addProperty() throws IOException {
		DialogOpener.openDialogWindow(sharedContext.getMainStage(), "/dialogs/add_property.fxml", "Add Property");
	}
	
	@FXML
	private void scrollUp() {
		
	}
	
	@FXML
	private void scrollDown() {
		
	}
	
	
	private void setListeners() {
        project.currentModelProperty().addListener((obs, oldModel, newModel) -> {
            if (newModel != null) {
                // Retrieve the list of Uncategorised Parameters from the new model.
                Platform.runLater(() -> {
                    propertyListView.setItems(newModel.getProperties());
                });
            }
        });
	}
	
	private void setCells() {
		propertyListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Property item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label label = new Label(item.getProperty()); // Display the model ID
                    label.setStyle(Font.UC_LIST_FONT); // Apply font styling
                    setGraphic(label); // Set the label as the cell's graphic
                    setText(null); // Clear any text (not needed with graphic)
                }
            }
        });
	}
}
