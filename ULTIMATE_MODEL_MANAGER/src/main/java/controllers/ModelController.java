package controllers;

import java.io.IOException;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import model.Model;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;
import utils.DialogOpener;
import utils.Font;

public class ModelController {
	
	@FXML private Button addButton;
	@FXML private Button upButton;
	@FXML private Button downButton;
	@FXML private ListView<Model> modelListView;
	
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
	    String filePath = DialogOpener.openPrismFileDialog(sharedContext.getMainStage());

	    if (filePath == null) {
	        // User canceled file selection; no need to proceed
	        return;
	    }

	    // Run model creation in a background thread
	    Task<Model> task = new Task<>() {
	        @Override
	        protected Model call() throws IOException {
	            return new Model(filePath); // File processing happens in the background
	        }
	    };

	    // Handle success (update UI safely on JavaFX thread)
	    task.setOnSucceeded(event -> {
	        Model newModel = task.getValue();
	        sharedContext.getProject().addModel(newModel);
	        System.out.println("Model added successfully: " + newModel);
	    });

	    // Handle failure (show error alert)
	    task.setOnFailed(event -> {
	        Throwable error = task.getException();
	        Alerter.showErrorAlert("Model Not Created!", "Error loading file: " + filePath + "\n" + error.getMessage());
	    });

	    // Start background task
	    new Thread(task).start();
	}

	
	@FXML
	public void scrollUp() {
        int selectedIndex = modelListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            // Select the item just above the current one
            modelListView.getSelectionModel().select(selectedIndex - 1);
        }
	}
	
	@FXML
	public void scrollDown() {
        int selectedIndex = modelListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < modelListView.getItems().size() - 1) {
            // Select the item just below the current one
            modelListView.getSelectionModel().select(selectedIndex + 1);
        }
	}
	
    /**
     * Configures the model list view, including its layout, behavior, and event handling.
     */
    private void setUpModelListView() {
    	
    	// When a new model is selected, the property is updated and the current model is set
    	modelListView.getSelectionModel().selectedItemProperty().addListener((obs, oldModel, newModel) -> {
    	    if (newModel != null) {
    	        sharedContext.getProject().setCurrentModel(newModel);
    	    }
    	});
    	
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
                    label.setStyle(Font.MODEL_LIST_FONT); // Apply font styling
                    setGraphic(label); // Set the label as the cell's graphic
                    setText(null); // Clear any text (not needed with graphic)
                }
            }
        });
    }
}
