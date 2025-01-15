package controllers;

import persistent_objects.Model;
import persistent_objects.SharedData;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Model_List {
	
	private String font = "-fx-font-size: 30px;";
	
	@FXML private ListView<Model> modelListView;
	@FXML private Button addModelButton;
	@FXML private Button upButton;
	@FXML private Button downButton;
	@FXML private VBox modelListVBox;
	
	private ObservableList<Model> models; // The list of models of the session
	private Stage mainStage; // The main stage of the application (for dialogs)
	
	@FXML
	private void initialize() {
	    // Fetch shared data from the SharedContext
        SharedData context = SharedData.getInstance();
        models = context.getModels();
        mainStage = context.getMainStage();
		
        setUpModelListView();
	}
	
    private void setUpModelListView() {
    	// find the grispane
        modelListVBox.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) { // Ensure the VBox is part of a scene
                Region grandParent = (Region) modelListVBox.getParent(); // Access the parent container
                Region gGrandParent = (Region) grandParent.getParent();
                if (gGrandParent != null) {
                    // Bind the width to 1/3 of the grandparent's width
                    grandParent.prefWidthProperty().bind(gGrandParent.widthProperty().multiply(1.0 / 3.0));
                }
            }
        });
    	// Behaviour and bindings for model list
    	modelListView.setItems(models);
    	modelListView.setCellFactory(param -> new ListCell<>() {
    	    @Override
    	    protected void updateItem(Model item, boolean empty) {
    	        super.updateItem(item, empty);
    	        if (empty || item == null) {
    	            setGraphic(null);
    	            setText(null);
    	        } else {
    	            Label label = new Label(item.getModelId());
    	            label.setStyle(font); // Set the font size
    	            setGraphic(label);
    	            setText(null);
    	        }
    	    }
    	});
    	
        // Add a listener to handle selection changes to model list and updates shared data
        modelListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
            	SharedData context = SharedData.getInstance(); // Call method with the newly selected model
            	context.setCurrentModel(newValue);
            	context.getParametersController().updateParameterDetails(newValue);
            }
        });
        
        // Add a key event handler to handle the delete key press
        // FIXME When the last model is deleted, the undefined parameters are still visible
        modelListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                Model selectedModel = modelListView.getSelectionModel().getSelectedItem();
                if (selectedModel != null) {
                    models.remove(selectedModel);
                }
            }
        });
    }
    
    @FXML
	private void handleAddModel() {
		// Open the dialog to add a new model
		String[] validFileTypes = {"*.ctmc", "*.dtmc", "*.pomdp", "*.prism"};
		File selectedFile = FileUtils.openFileDialog(mainStage, "Select Model File", "Prism Files", validFileTypes);
		if (selectedFile != null) {
			// create Model instance
			String id = selectedFile.getName().replaceFirst("[.][^.]+$", ""); // the id of the model
			String filePath = selectedFile.getAbsolutePath();
			Model model = new Model(id, filePath);
			models.add(model);
	        
			// Parse the file for undefined parameters using PrismFileParser
	        PrismFileParser parser = new PrismFileParser();
	        try {
	            List<String> undefinedParams = parser.parseFile(filePath);

	            if (undefinedParams != null) {
	                // For each undefined parameter, create an UndefinedParameter object
	                for (String param : undefinedParams) {;
	                        model.addUndefinedParameter(param);
	                    }
	                }
	            }
	         catch (IOException e) {
	             e.printStackTrace(); // Log parsing errors for debugging purposes
	         }	
		}
		
		else {
			Alerter.showAlert("Invalid file selected", "Please select a valid model file");
		}
	}
	
    @FXML
	private void handleUp() {
        // Handle the "Up" button action
        int selectedIndex = modelListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            // Select the item just above the current one
            modelListView.getSelectionModel().select(selectedIndex - 1);
        }
	}
	
    @FXML
	private void handleDown() {
        // Handle the "Down" button action
        int selectedIndex = modelListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < models.size() - 1) {
            // Select the item just below the current one
            modelListView.getSelectionModel().select(selectedIndex + 1);
        }
	}
}