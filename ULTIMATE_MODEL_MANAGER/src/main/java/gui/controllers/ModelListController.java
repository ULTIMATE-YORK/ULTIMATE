package gui.controllers;

import model.persistent_objects.*;
import utils.Alerter;
import utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import utils.PrismFileParser;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for managing the model list in the application.
 * This class handles the display, addition, and deletion of models in the session.
 * It also facilitates navigation and selection updates for models.
 */
public class ModelListController extends Controller {

    // Style for labels displayed in the list
    private String font = "-fx-font-size: 18px;";

    // FXML-linked fields for UI components
    @FXML private ListView<Model> modelListView; // List view to display models
    @FXML public VBox modelListVBox; // Container for the model list view

    // Reference to the main stage of the application
    private Stage mainStage;
    
    private SharedData context;

    /**
     * Initializes the controller. This method is called automatically after the FXML file is loaded.
     * Fetches shared data (models and stage) and sets up the list view.
     */
    @FXML
    private void initialize() {
        // Fetch shared data from the singleton SharedData instance
        context = SharedData.getInstance();
        mainStage = context.getMainStage(); // Load the primary stage of the application

        // Set up behavior and appearance of the model list view
        setUpModelListView();
        registerController();
    }

    /**
     * Configures the model list view, including its layout, behavior, and event handling.
     */
    private void setUpModelListView() {
        // Dynamically adjust the width of the VBox based on the parent container

        // Bind the model list to the observable list of models
        modelListView.setItems(context.getModels());

        // Customize the appearance of each item in the list view
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

        // Add a listener for selection changes in the list view
        modelListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Update the views
            	context.setCurrentModel(newValue);
                context.update();
            }
        });

        // Add a key event handler to allow deletion of models using the Backspace key
        modelListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                Model selectedModel = modelListView.getSelectionModel().getSelectedItem();
                if (selectedModel != null) {
                    context.getModels().remove(selectedModel); // Remove the selected model
                }
            }
        });
    }

    /**
     * Handles the addition of a new model to the list.
     * Opens a file dialog for selecting a model file and parses the file for undefined parameters.
     */
    @FXML
    private void handleAddModel() {
        // Supported file types for model files
        String[] validFileTypes = {"*.ctmc", "*.dtmc", "*.pomdp", "*.prism"};
        // Open a file dialog for selecting a model file
        FileUtils.openFileDialog(mainStage, "Select Model File", "Prism Files", validFileTypes, selectedFile -> {
	        if (selectedFile != null) {
	            // Create a new Model instance with the file details
	            String id = selectedFile.getName().replaceFirst("[.][^.]+$", ""); // Extract the file name without extension
	            String filePath = selectedFile.getAbsolutePath();
	            Model model = new Model(id, filePath);
	            // if first model added, register it and select it
	            if (context.getModels().isEmpty()) {
	            	context.getModels().add(model); // Add the model to the list
	            	context.setCurrentModel(model);
	            	handleDown(); // select the model
	            }
	            else {
	                context.getModels().add(model); // Add the model to the list
	            }
	            // Parse the file for undefined parameters using PrismFileParser
	            PrismFileParser parser = new PrismFileParser();
	            try {
	                List<String> undefinedParams = parser.parseFile(filePath);
	
	                if (undefinedParams != null) {
	                    // Add each undefined parameter to the model
	                    for (String param : undefinedParams) {
	                        model.addUndefinedParameter(param);
	                    }
	                }
	            } catch (IOException e) {
	                // Log any errors during file parsing
	                e.printStackTrace();
	            }
	        } else {
	            // Show an alert if the selected file is invalid
	            Alerter.showAlert("Invalid file selected", "Please select a valid model file");
	        }
        });
    }

    /**
     * Handles the "Up" button action. Moves the selection in the list view up by one item.
     */
    @FXML
    private void handleUp() {
        int selectedIndex = modelListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            // Select the item just above the current one
            modelListView.getSelectionModel().select(selectedIndex - 1);
        }
    }

    /**
     * Handles the "Down" button action. Moves the selection in the list view down by one item.
     */
    @FXML
    private void handleDown() {
        int selectedIndex = modelListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < context.getModels().size() - 1) {
            // Select the item just below the current one
            modelListView.getSelectionModel().select(selectedIndex + 1);
        }
    }

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerController() {
		context.registerController(this);	}
}