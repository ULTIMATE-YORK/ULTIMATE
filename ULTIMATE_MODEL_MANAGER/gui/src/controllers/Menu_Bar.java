package controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import utils.Alerter;
import utils.FileUtils;
import utils.ModelUtils;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import persistent_objects.Model;
import persistent_objects.SharedData;

public class Menu_Bar {
	
	private File saveFile; // location of the current save file
	
	// Define FXML-linked fields for UI components
    @FXML private MenuItem loadItem;
    @FXML private MenuItem saveItem;
    @FXML private MenuItem saveAsItem;
    @FXML private MenuItem quitItem;
    // TODO implement handlers for menu items for the 'Verification' menu
    @FXML private MenuItem addProperty;
    @FXML private MenuItem loadPropertyList;
    @FXML private MenuItem savePropertyList;
    @FXML private MenuItem saveAsPropertyList;
    @FXML private MenuItem verifyProperty;
    @FXML private MenuItem deleteProperty;
    @FXML private MenuItem editProperty;
    
    // Here we define the models (permanent data) that the controller will need access to
    private ObservableList<Model> models; // The list of models of the session
    private Stage mainStage; // The main stage of the application (for dialogs)
    
    @FXML
	private void initialize() {
        // Fetch shared data from the SharedContext
        SharedData context = SharedData.getInstance();
        models = context.getModels();
        mainStage = context.getMainStage();
	}
    
    // FIXME if there is a current session, the user should be prompted to save it
	@FXML
	private void handleLoad() {
        File file = FileUtils.openFileDialog(mainStage, "Load Models", "loading JSON files", "*.json");
        if (file != null) {
            try {
                // Update title
                mainStage.setTitle(file.getName().replaceAll(".json", ""));
                JSONObject root = FileUtils.parseJSONFile(file);
                ModelUtils.parseAndInitializeModels(root, models);
            } catch (IOException e) {
                Alerter.showAlert("Error loading file: " + e.getMessage(), "Failed to load models");
            } catch (JSONException e) {
                Alerter.showAlert("Invalid JSON format: " + e.getMessage(), "Failed to load models");
            }
        }
	}

	@FXML
	private void handleSave() {
        List<Model> modelList = models;  // Convert ObservableList to List
        if (saveFile != null) {
        	ModelUtils.saveModelsToFile(modelList, saveFile.getAbsolutePath());
        }
        else {
        	handleSaveAs();
        }
	}

	@FXML
	private void handleSaveAs() {
        // Convert ObservableList to List
        List<Model> modelList = models;

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(mainStage);  // Let user choose file path

        if (file != null) {
            // Update saveFile so it can be reused for future saves
            saveFile = file;
            ModelUtils.saveModelsToFile(modelList, file.getAbsolutePath());  // Save the models to the file
        }
	}

	@FXML
	private void handleQuit() {
	    // FIXME if there is a current session, the user should be prompted to save it
		mainStage.close();
	}

	// TODO implement the menu item handlers for the 'Verification' menu
	@FXML
	private void handleAddProperty() {

	}

	@FXML
	private void handleLoadPropertyList() {

	}

	@FXML
	private void handleSavePropertyList() {

	}

	@FXML
	private void handleSaveAsPropertyList() {

	}

	@FXML
	private void handleVerifyProperty() {

	}

	@FXML
	private void handleDeleteProperty() {

	}

	@FXML
	private void handleEditProperty() {

	}
}
