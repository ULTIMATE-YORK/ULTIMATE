package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import model.Model;
import project.Project;
import sharedContext.SharedContext;

public class ModelController {
	
	@FXML private Button addButton;
	@FXML private Button upButton;
	@FXML private Button downButton;
	@FXML private ListView<Model> modelListView;
	
    // Style for labels displayed in the list
    private String font = "-fx-font-size: 18px;";
	
	private SharedContext sharedContext = SharedContext.getInstance();

    @FXML
    public void initialize() {
        // Get the shared project instance
        Project project = sharedContext.getProject();
        // Bind the ListView to the project's observable models
        modelListView.setItems(project.getObservableModels());
        setUpModelListView();
    }
	
	@FXML
	public void addModel() {
		//sharedContext.getProject().addModel(new Model());
	}
	
	@FXML
	public void scrollUp() {
		
	}
	
	@FXML
	public void scrollDown() {

	}
	
    /**
     * Configures the model list view, including its layout, behavior, and event handling.
     */
    private void setUpModelListView() {
        // Customise the appearance of each item in the list view
        modelListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Model item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label label = new Label(item.getModelId()); // Display the model ID
                    label.setStyle(font); // Apply font styling
                    setGraphic(label); // Set the label as the cell's graphic
                    setText(null); // Clear any text (not needed with graphic)
                }
            }
        });
    }
}
