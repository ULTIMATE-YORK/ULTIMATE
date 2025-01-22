package gui.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import utils.*;
import verification_engine.prism.PrismAPI;
import verification_engine.storm.OSCommandExecutor;
import verification_engine.storm.StormAPI;
import javafx.application.Platform;
import javafx.collections.ObservableList; // JavaFX observable list for binding data
import javafx.event.ActionEvent;
import javafx.fxml.FXML; // JavaFX annotation for linking UI elements
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem; // JavaFX menu item class
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser; // JavaFX file chooser for file selection dialogs
import javafx.stage.Stage; // JavaFX stage class representing the main application window
import model.persistent_objects.*; // Custom class representing a model
import prism.PrismException;

/**
 * Controller for handling the functionality of the menu bar in the application.
 * It manages file operations (load, save, save as) and prepares for property-related actions.
 */
public class MenuBarController extends Controller {
	
	// The file currently associated with the session (for saving/loading)
	private File saveFile;

    @FXML private MenuItem choosePrism;
    @FXML private MenuItem chooseStorm;
    
    // Observable list of models representing the session data
    private ObservableList<Model> models;
    // The primary stage of the application, used for displaying dialogs
    private Stage mainStage;
    
    private SharedData context;
   
    /**
     * Initializes the controller. Called automatically after the FXML file is loaded.
     * Fetches shared data (models and stage) from the shared context.
     */
    @FXML
	private void initialize() {
        // Obtain shared data from the singleton SharedData instance
        context = SharedData.getInstance();
        models = context.getModels(); // Load the session's list of models
        mainStage = context.getMainStage(); // Load the primary stage of the application
        registerController();
        setUpMenuActions();
	}
    
    private void setUpMenuActions() {
    	choosePrism.addEventFilter(ActionEvent.ACTION, event -> {
    		context.setPMCEngine("PRISM");
    	});
    	chooseStorm.addEventFilter(ActionEvent.ACTION, event -> {
    		if (context.getStormInstallation() == "") {
    			Alerter.showAlert("No STORM installation found!", "Please configure STORM in Options -> cofigure storm");
    		}
    		else {
        		context.setPMCEngine("STORM");
    		}
    	});
    }
    
    /**
     * Handles the 'Load' menu item. Opens a file chooser dialog to load a JSON file
     * containing model data. If the file is valid, it initializes the models and updates the UI.
     */
	@FXML
	private void handleLoad() {
        // Open a file dialog for selecting a JSON file
        File file = FileUtils.openFileDialog(mainStage, "Load Models", "loading JSON files", "*.json");
        if (file != null) {
            try {
                // Update the window title with the file name (excluding the extension)
            	Platform.runLater(() -> mainStage.setTitle(file.getName().replaceAll(".json", "")));                
                // Parse the selected JSON file and initialize models
                JSONObject root = FileUtils.parseJSONFile(file);
                ModelUtils.parseAndInitializeModels(root, models);
            } catch (IOException e) {
                // Show an alert if there is an error reading the file
                Platform.runLater( () -> Alerter.showAlert("Error loading file: " + e.getMessage(), "Failed to load models"));
            } catch (JSONException e) {
                // Show an alert if the JSON file has an invalid format
                Platform.runLater( () -> Alerter.showAlert("Invalid JSON format: " + e.getMessage(), "Failed to load models"));
            }
        }
	}

    /**
     * Handles the 'Save' menu item. Saves the current models to the file associated with
     * the session. If no file is associated, prompts the user to select a location.
     */
	@FXML
	private void handleSave() {
        List<Model> modelList = models;  // Convert the observable list to a standard list
        if (saveFile != null) {
        	// Save models to the existing save file
        	ModelUtils.saveModelsToFile(modelList, saveFile.getAbsolutePath());
        } else {
        	// Prompt the user to select a file location if none is set
        	handleSaveAs();
        }
	}

    /**
     * Handles the 'Save As' menu item. Prompts the user to select a file location
     * and saves the current models to the chosen file.
     */
	@FXML
	private void handleSaveAs() {
        // Convert the observable list to a standard list
        List<Model> modelList = models;

        // Open a file chooser dialog for selecting a save location
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(mainStage);

        if (file != null) {
            // Update the saveFile reference for future saves
            saveFile = file;
            // Save the models to the selected file
            ModelUtils.saveModelsToFile(modelList, file.getAbsolutePath());
        }
	}

    /**
     * Handles the 'Quit' menu item. Closes the application.
     * TODO: Add functionality to prompt the user to save the current session if unsaved changes exist.
     */
	@FXML
	private void handleQuit() {
	    mainStage.close(); // Close the main stage (application window)
	}

	// TODO implement the menu item handlers for the 'Verification' menu
	@FXML
	private void handleAddProperty() throws IOException {
		Model model = context.getCurrentModel();
		if (model == null) {
			Platform.runLater( () -> Alerter.showAlert("Error", "Please select a model from the list to add a property!"));
			return;
		}
		else {
			// FIXME make this thread safe
			DialogLoader.load("/dialogs/add_property.fxml", "Add Property", context.getPropertiesController());
		}
	}

	@FXML
	private void handleLoadPropertyList() throws IOException {
		// FIXME make this thread safe
        File file = FileUtils.openFileDialog(mainStage, "Load Properties File", "loading Properties files", "*.pctl");
        if (file != null) {
        	context.getCurrentModel().setPropFile(file.getAbsolutePath());
        	context.update();
        }
        else {
        	Platform.runLater( () -> Alerter.showAlert("No file Found", "Aborting..."));
        }
	}

	@FXML
	private void handleSavePropertyList() throws IOException {
		// check if the current model has a prop file
		if (context.getCurrentModel().hasPropFile()) {
			// FIXME thread safe?
			FileUtils.updatePropertyFile(context.getCurrentModel());
		}
		else {
			handleSaveAsPropertyList();
		}
	}

	@FXML
	private void handleSaveAsPropertyList() throws IOException {
		// FIXME thread-safe?
        // Open a file chooser dialog for selecting a save location
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Property Files", "*.pctl"));
        File file = fileChooser.showSaveDialog(mainStage);
        
        if (file != null && !context.getCurrentModel().hasPropFile()) {
        	FileUtils.generatePropertyFile(file.getAbsolutePath(), context.getCurrentModel().getProperties());
        }
        
        // set the file for the current model
        context.getCurrentModel().setPropFile(file.getAbsolutePath());
	}

	@FXML
	private void handleVerifyProperty() throws FileNotFoundException, PrismException {
		if (context.getPMCEngine() == "PRISM") {
			PrismAPI.run(context.getCurrentModel(), context.getCurrentModel().getPropFile());
		}
		else {
			StormAPI.run(context.getCurrentModel(), context.getCurrentModel().getPropFile(), context.getStormInstallation());
		}
	}

	@FXML
	private void handleDeleteProperty() {

	}

	@FXML
	private void handleEditProperty() {

	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerController() {
		context.registerController(this);
	}
	
	@FXML
	private void configureStorm() {
		if (context.getStormInstallation() != "") {
			Platform.runLater( () -> Alerter.showAlert("STORM has been configure already!", "STORM found at: " + context.getStormInstallation()));
		}
		else {
			// FIXME thread-safe?
			File file = FileUtils.openDirectoryDialog(mainStage, "Locate STORM installation");
			if (file != null) {
				context.setStormInstallation(file.getAbsolutePath());
			}
		}
	}
}
