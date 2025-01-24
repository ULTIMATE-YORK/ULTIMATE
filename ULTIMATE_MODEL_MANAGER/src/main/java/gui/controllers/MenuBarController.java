package gui.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.json.JSONException;
import org.json.JSONObject;

import utils.*;
import verification_engine.prism.PrismAPI;
import verification_engine.storm.StormAPI;
import javafx.application.Platform;
import javafx.collections.ObservableList; // JavaFX observable list for binding data
import javafx.event.ActionEvent;
import javafx.fxml.FXML; // JavaFX annotation for linking UI elements
import javafx.scene.control.MenuItem; // JavaFX menu item class
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
     * Initialises the controller. Called automatically after the FXML file is loaded.
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
     * containing model data. If the file is valid, it initialises the models and updates the UI.
     */
    @FXML
    private void handleLoad() {
        // Open a file dialog for selecting a JSON file
        FileUtils.openFileDialog(mainStage, "Load Models", "loading JSON files", "*.json", file -> {
            if (file != null) {
                // Update the window title with the file name (excluding the extension)
                Platform.runLater(() -> mainStage.setTitle(file.getName().replaceAll(".json", "")));
                // Parse the selected JSON file asynchronously
                FileUtils.parseJSONFileAsync(file, new FileUtils.JSONCallback() {
                    @Override
                    public void onJSONParsed(JSONObject root) {
                        try {
                            // Process the parsed JSON and initialise models
                            ModelUtils.parseAndInitializeModels(root, models);
                        } catch (JSONException e) {
                            // Show an alert if the JSON parsing fails during initialisation
                            Platform.runLater(() -> Alerter.showAlert(
                                    "Invalid JSON structure: " + e.getMessage(),
                                    "Failed to load models"
                            ));
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        // Handle exceptions (IOException, JSONException, etc.)
                        Platform.runLater(() -> Alerter.showAlert(
                                "Error reading JSON file: " + e.getMessage(),
                                "Failed to load models"
                        ));
                    }
                });
            } else {
                // Handle case where no file was selected
                Platform.runLater(() -> Alerter.showAlert("No file selected.", "Load Models"));
            }
        });
    }

    /**
     * Handles the 'Save' menu item. Saves the current models to the file associated with
     * the session. If no file is associated, prompts the user to select a location.
     */
	@FXML
	private void handleSave() {
	    List<Model> modelList = models;  // Convert the observable list to a standard list

	    if (saveFile != null) {
	        // Save models asynchronously to the existing save file
	        ModelUtils.saveModelsToFileAsync(modelList, saveFile.getAbsolutePath(), new ModelUtils.FileSaveCallback() {
	            @Override
	            public void onSuccess() {
	                // Notify the user on the JavaFX thread that the save operation was successful
	                Platform.runLater(() -> Alerter.showAlert("Models saved successfully!", "Save Complete"));
	            }

	            @Override
	            public void onError(Throwable e) {
	                // Notify the user of an error that occurred during saving
	                Platform.runLater(() -> Alerter.showAlert("Error saving models: " + e.getMessage(), "Save Failed"));
	            }
	        });
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

	    // Open the file chooser dialog on the JavaFX Application Thread
	    Platform.runLater(() -> {
	        FileChooser fileChooser = new FileChooser();
	        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
	        
	        // Show the save dialog and handle file selection asynchronously
	        File file = fileChooser.showSaveDialog(mainStage);

	        if (file != null) {
	            // Update the saveFile reference for future saves
	            saveFile = file;

	            // Save the models asynchronously to the selected file
	            ModelUtils.saveModelsToFileAsync(modelList, file.getAbsolutePath(), new ModelUtils.FileSaveCallback() {
	                @Override
	                public void onSuccess() {
	                    // Notify the user of successful save on the JavaFX thread
	                    Platform.runLater(() -> Alerter.showAlert("Models saved successfully!", "Save Complete"));
	                }

	                @Override
	                public void onError(Throwable e) {
	                    // Notify the user of an error that occurred during saving
	                    Platform.runLater(() -> Alerter.showAlert("Error saving models: " + e.getMessage(), "Save Failed"));
	                }
	            });
	        } else {
	            // Notify the user that save was cancelled
	            Platform.runLater(() -> Alerter.showAlert("Save operation canceled.", "Save Models"));
	        }
	    });
	}

    /**
     * Handles the 'Quit' menu item. Closes the application.
     * TODO: Add functionality to prompt the user to save the current session if unsaved changes exist.
     */
	@FXML
	private void handleQuit() {
	    mainStage.close(); // Close the main stage (application window)
	}

    /**
     * Handles the addition of a new property by loading the property dialog.
     * Ensures thread safety by running UI operations on the JavaFX thread.
     * 
     * @throws IOException if the dialog fails to load
     */
    @FXML
    private void handleAddProperty() throws IOException {
        Model model = context.getCurrentModel();
        if (model == null) {
            // Ensure UI updates happen on the JavaFX thread
            Platform.runLater(() -> Alerter.showAlert("Error", "Please select a model from the list to add a property!"));
            return;
        } else {
            // Load the property addition dialog safely on the JavaFX thread
            Platform.runLater(() -> {
                try {
                    DialogLoader.load("/dialogs/add_property.fxml", "Add Property", context.getPropertiesController());
                } catch (IOException e) {
                    Alerter.showAlert("Error", "Failed to load property dialog: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Handles loading a property list file asynchronously and updates the model.
     * Ensures file operations do not block the UI thread.
     * 
     * @throws IOException if file loading fails
     */
    @FXML
    private void handleLoadPropertyList() throws IOException {
        // Open the file dialog asynchronously to avoid blocking UI thread
        FileUtils.openFileDialog(mainStage, "Load Properties File", "loading Properties files", "*.pctl", file -> {
            if (file != null) {
                // Safely update the model's property file on the JavaFX thread
                Platform.runLater(() -> {
                    try {
						context.getCurrentModel().setPropFile(file.getAbsolutePath());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    context.update();
                });
            } else {
                Platform.runLater(() -> Alerter.showAlert("No file Found", "Aborting..."));
            }
        });
    }

    /**
     * Handles saving the current model's property list asynchronously.
     * Ensures that file writing does not block the UI thread.
     * 
     * @throws IOException if file saving fails
     */
    @FXML
    private void handleSavePropertyList() throws IOException {
        // Check if the model has a property file set
        if (context.getCurrentModel().hasPropFile()) {
            // Save properties asynchronously using the correct method
            FileUtils.updatePropertyFileAsync(context.getCurrentModel(), new FileUtils.TaskCallback() {
                @Override
                public void onSuccess() {
                    Platform.runLater(() -> Alerter.showAlert("Success", "Property list saved successfully!"));
                }

                @Override
                public void onError(Throwable e) {
                    Platform.runLater(() -> Alerter.showAlert("Error", "Failed to save property list: " + e.getMessage()));
                }
            });
        } else {
            handleSaveAsPropertyList();
        }
    }

    /**
     * Handles "Save As" for the property list, prompting the user for a save location.
     * Runs the file dialog on the JavaFX thread and saves asynchronously.
     * 
     * @throws IOException if the save dialog fails
     */
    @FXML
    private void handleSaveAsPropertyList() throws IOException {
        // Open the file chooser on the JavaFX thread
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Property Files", "*.pctl"));
            File file = fileChooser.showSaveDialog(mainStage);

            if (file != null) {
                // Save properties asynchronously
                FileUtils.generatePropertyFileAsync(file.getAbsolutePath(), context.getCurrentModel().getProperties(), new FileUtils.TaskCallback() {
                    @Override
                    public void onSuccess() {
                        Platform.runLater(() -> Alerter.showAlert("Success", "Property list saved successfully!"));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Platform.runLater(() -> Alerter.showAlert("Error", "Failed to save property list: " + e.getMessage()));
                    }
                });

                // Safely update the model's property file reference on the JavaFX thread
                Platform.runLater(() -> {
					try {
						context.getCurrentModel().setPropFile(file.getAbsolutePath());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				});
            } else {
                Platform.runLater(() -> Alerter.showAlert("Cancelled", "Save operation was cancelled."));
            }
        });
    }

    /**
     * Verifies the current model's property list using the selected verification engine.
     * Runs verification asynchronously to prevent UI blocking.
     * 
     * @throws FileNotFoundException if the property file is missing
     * @throws PrismException if the verification fails
     */
    @FXML
    private void handleVerifyProperty() throws FileNotFoundException, PrismException {
        Model model = context.getCurrentModel();
        String propFilePath = model.getPropFile();

        if (context.getPMCEngine().equalsIgnoreCase("PRISM")) {
            CompletableFuture.runAsync(() -> {
                try {
                    PrismAPI.run(model, propFilePath);
                    Platform.runLater(() -> Alerter.showAlert("Success", "Verification completed using PRISM."));
                } catch (Exception e) {
                    Platform.runLater(() -> Alerter.showAlert("Error", "Verification failed: " + e.getMessage()));
                }
            });
        } else {
            CompletableFuture.runAsync(() -> {
                try {
                    StormAPI.run(model, propFilePath, context.getStormInstallation());
                    Platform.runLater(() -> Alerter.showAlert("Success", "Verification completed using Storm."));
                } catch (Exception e) {
                    Platform.runLater(() -> Alerter.showAlert("Error", "Verification failed: " + e.getMessage()));
                }
            });
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
	
	/**
	 * Handles the configuration of the STORM model-checking tool.
	 * Ensures that UI updates and file operations are performed in a thread-safe manner.
	 */
	@FXML
	private void configureStorm() {
	    // Check if STORM is already configured
	    if (!context.getStormInstallation().isEmpty()) {
	        // Ensure UI updates happen on the JavaFX thread
	        Platform.runLater(() -> Alerter.showAlert("STORM has already been configured!", 
	                "STORM found at: " + context.getStormInstallation()));
	    } else {
	        // Open the directory chooser asynchronously to prevent blocking the UI thread
	        FileUtils.openDirectoryDialog(mainStage, "Locate STORM installation", file -> {
	            if (file != null) {
	                // Safely update the STORM installation path on the JavaFX thread
	                Platform.runLater(() -> context.setStormInstallation(file.getAbsolutePath()));
	            } else {
	                // Notify the user if the operation was cancelled
	                Platform.runLater(() -> Alerter.showAlert("Configuration canceled", "No STORM installation selected."));
	            }
	        });
	    }
	}
}
