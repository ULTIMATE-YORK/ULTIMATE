package com.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.parameters.DependancyParameter;
import com.parameters.EnvironmentParameter;
import com.parameters.InternalParameter;
import com.parameters.Model;
import com.parameters.Parameter;
import com.parameters.UndefinedParameter;
import com.ultimate.modelmanager.utils.FileUtils;
import com.ultimate.modelmanager.utils.ParameterUtils;
import com.ultimate.modelmanager.utils.PrismFileParser;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Controller {
	
	// Initialised in Main -> the list containing models and the root window
	private final ObservableList<Model> models;
	private final Stage mainStage;
	
    // The location of the last save
	File saveFile;
	// The name of the project
	String projectName = "untitled";
	// The list of model names as strings
	String[] modelNames;
	private String font = "-fx-font-size: 30px;";
	private String font2 = "-fx-font-size: 20px;";
	
	// Define FXML-linked fields for UI components
    @FXML private MenuItem loadItem;
    @FXML private MenuItem saveItem;
    @FXML private MenuItem saveAsItem;
    @FXML private MenuItem quitItem;

    @FXML private Button addModelButton;
    @FXML private Button upButton;
    @FXML private Button downButton;
    @FXML private Button addEnvironmentParamButton;
    @FXML private Button addDependencyParamButton;
    @FXML private Button addInternalParamButton;

    @FXML private ListView<Model> modelListView;
    @FXML private Label modelDetails;

    @FXML private ListView<EnvironmentParameter> eParamList;
    @FXML private ListView<DependancyParameter> dParamList;
    @FXML private ListView<InternalParameter> iParamList;
    @FXML private ListView<UndefinedParameter> uParamList;
    
    public Controller(Stage mainStage, ObservableList<Model> models) {
    	this.mainStage = mainStage;
    	this.models = models;
    }
    
    // Initialisation method
    @FXML
    private void initialize() {
        
    	// Set up initial behaviour and bindings:
    	setUpMenuItems();
    	setUpButtons();
    	setUpModelListView();
    	
    	// Set up list view cell factories
    	setUpListViewCellFactory(eParamList);
    	setUpListViewCellFactory(dParamList);
    	setUpListViewCellFactory(iParamList);
      setUpListViewCellFactory(uParamList);
}

// Helper method to set up list view cell factories for environment, dependency, and internal parameters
private <T extends Parameter> void setUpListViewCellFactory(ListView<T> listView) {
    listView.setCellFactory(param -> new ListCell<>() {
        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else if (item instanceof EnvironmentParameter || item instanceof DependancyParameter || item instanceof InternalParameter) {
                setGraphic(createCellLayout(item));
                setText(null);
            } else {
                Label label = new Label(item.getName());
                label.setStyle(font); // Set the font size to 30px
                setGraphic(label);
                setText(null);
            }
        }
    });
}
    
    private void setUpButtons() {
        addModelButton.setOnAction(e -> {
			try {
				handleAddModel(mainStage);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
        upButton.setOnAction(e -> handleUpButton());
        downButton.setOnAction(e -> handleDownButton());
        addEnvironmentParamButton.setOnAction(e -> handleParam(mainStage, getCurrentModel(), "Environment", null));
        addDependencyParamButton.setOnAction(e -> handleParam(mainStage, getCurrentModel(), "Dependency", null));
        addInternalParamButton.setOnAction(e -> handleParam(mainStage, getCurrentModel(), "Internal", null));
    }
    
    private void updateButtonStates(Model model) {
        boolean hasUndefinedParameters = !model.getUndefinedParameters().isEmpty();
        addEnvironmentParamButton.setVisible(hasUndefinedParameters);
        addDependencyParamButton.setVisible(hasUndefinedParameters);
        addInternalParamButton.setVisible(hasUndefinedParameters);
    }
    
    private String[] getModelIDs() {
		modelNames = new String[models.size()];
		for (int i = 0; i < models.size(); i++) {
			modelNames[i] = models.get(i).getModelId();
		}
		return modelNames;
    }
    
    private void setUpModelListView() {
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
    	
        // Add a listener to handle selection changes to model list
        modelListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateModelDetails(newValue); // Call method with the newly selected model
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
    
    private Model getCurrentModel() {
    	return modelListView.getSelectionModel().getSelectedItem();
    }
    
    private void setUpMenuItems() {
        loadItem.setOnAction(e -> handleLoad());
        saveItem.setOnAction(e -> handleSave());
        saveAsItem.setOnAction(e -> handleSaveAs());
        quitItem.setOnAction(e -> handleQuit());
    }
    
    private Stage createEditorStage(Stage owner, String stageName) {
    	Stage editorStage = new Stage();
    	editorStage.setTitle(stageName);
    	editorStage.initOwner(owner);
    	return editorStage;
    }
    
    private void handleAddModel(Stage mainStage) throws IOException {
        Stage editorStage = createEditorStage(mainStage, "Add New Model");
        
        // create a AddModelController instance to handle dialog
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ultimatemodelmanager/AddModelDialog.fxml"));
        loader.setController(new AddModelController(mainStage, editorStage, models));
        
        // TODO prevent window from opening if file dialog is cancelled
        // loading root element of AddModelDialog.fxml
        VBox editorLayout = loader.load();

        // Set up the scene
        Scene scene = new Scene(editorLayout);
        editorStage.setScene(scene);
        editorStage.showAndWait();
    }
    
    private void handleUpButton() {
        // Handle the "Up" button action
        int selectedIndex = modelListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            // Select the item just above the current one
            modelListView.getSelectionModel().select(selectedIndex - 1);
        }
    }
    
    private void handleDownButton() {
        // Handle the "Down" button action
        int selectedIndex = modelListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < models.size() - 1) {
            // Select the item just below the current one
            modelListView.getSelectionModel().select(selectedIndex + 1);
        }
    }
    
    private void handleLoad() {
        File file = FileUtils.openFileDialog(mainStage, "Load Models", "loading JSON files", "*.json");
        if (file != null) {
            try {
                // Update title
                projectName = file.getName().replaceAll(".json", "");
                mainStage.setTitle(projectName);
                JSONObject root = FileUtils.parseJSONFile(file);
                models.clear();
                parseAndInitializeModels(root);
            } catch (IOException e) {
                showAlert("Error loading file: " + e.getMessage(), "Failed to load models");
            } catch (JSONException e) {
                showAlert("Invalid JSON format: " + e.getMessage(), "Failed to load models");
            }
        }
        handleDownButton(); // so a model is selected after loading
    }

    private void parseAndInitializeModels(JSONObject root) {
        JSONObject modelsObject = root.getJSONObject("models");
        modelsObject.keySet().forEach(modelId -> {
            Model model = initializeModel(modelsObject.getJSONObject(modelId));
            models.add(model);
        });
    }

    private Model initializeModel(JSONObject modelJson) {
        String id = modelJson.getString("id");
        String filePath = modelJson.getString("fileName");
        Model model = new Model(id, filePath);

        JSONObject parametersObject = modelJson.getJSONObject("parameters");
        // Define the array of values
        String[] parameterTypes = {"environment", "dependency", "internal"};

        // Iterate over the array and call the method
        for (String parameterType : parameterTypes) {
            ParameterUtils.deserializeParameters(parametersObject, model, parameterType);
        }
        addUndefinedParameters(model);
        return model;
    }
    
    private void addUndefinedParameters(Model model) {
       	PrismFileParser parser = new PrismFileParser();
        // Parse the model's file with PrismFileParser
        List<String> parsedParams = null;
		try {
			parsedParams = parser.parseFile(model.getFilePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String parsedParam : parsedParams) {
			if (!model.isParam(parsedParam)) {
				model.addUndefinedParameter(parsedParam);
			}
		}
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void handleSave() {
        List<Model> modelList = models;  // Convert ObservableList to List
        if (saveFile != null) {
        	saveModelsToFile(modelList, saveFile.getAbsolutePath());
        }
        else {
        	handleSaveAs();
        }
    }
    
    // TODO: re-factor (create file dialog method)
    private void handleSaveAs() {
        // Convert ObservableList to List
        List<Model> modelList = models;

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(mainStage);  // Let user choose file path

        if (file != null) {
            // Update saveFile so it can be reused for future saves
            saveFile = file;
            saveModelsToFile(modelList, file.getAbsolutePath());  // Save the models to the file
        }
    }
    
    private void handleQuit() {
    	mainStage.close();
    }
    
    private void handleParam(Stage mainStage, Model currentModel, String paramType, Parameter parameter) {
        Stage editorStage = new Stage();
        editorStage.setTitle((parameter == null ? "Add New " : "Edit ") + paramType + " Parameter");
        
        FXMLLoader loader = new FXMLLoader();
        switch (paramType) {
            case "Environment":
                loader.setLocation(getClass().getResource("/com/ultimatemodelmanager/EParamDialog.fxml"));
                break;
            case "Dependency":
                loader.setLocation(getClass().getResource("/com/ultimatemodelmanager/DParamDialog.fxml"));
                break;
            case "Internal":
                loader.setLocation(getClass().getResource("/com/ultimatemodelmanager/IParamDialog.fxml"));
                break;
            default:
                throw new IllegalArgumentException("Unknown parameter type: " + paramType);
        }
        loader.setController(new ParameterEditorController(editorStage, currentModel, parameter, paramType, getModelIDs()));
        
        VBox editorLayout = null;
		try {
			editorLayout = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        // Set up the scene
        Scene scene = new Scene(editorLayout);
        editorStage.setScene(scene);
        editorStage.initOwner(mainStage);
        editorStage.showAndWait();
        
        updateModelDetails(currentModel);
    }

    private void handleEditItem(Parameter item) {
		if (item instanceof EnvironmentParameter) {
			handleParam(mainStage, getCurrentModel(), "Environment", item);
		} else if (item instanceof DependancyParameter) {
			handleParam(mainStage, getCurrentModel(), "Dependency", item);
		} else if (item instanceof InternalParameter) {
			handleParam(mainStage, getCurrentModel(), "Internal", item);
		}
    }
 
    private void handleRemoveItem(Parameter item) {
		String name = item.getName();
    	if (item instanceof EnvironmentParameter) {
    		getCurrentModel().removeEnvironmentParamter((EnvironmentParameter) item);
    		getCurrentModel().addUndefinedParameter(name);
    	}
		else if (item instanceof DependancyParameter) {
			getCurrentModel().removeDependencyParameter((DependancyParameter) item);
    		getCurrentModel().addUndefinedParameter(name);
		} else if (item instanceof InternalParameter) {
			getCurrentModel().removeInternalParameter((InternalParameter) item);
    		getCurrentModel().addUndefinedParameter(name);
		}
    	updateModelDetails(getCurrentModel());
    }
    
    private void updateModelDetails(Model model) {
        // Update the model ID and file path in the Text component
        Label modelIDFile = (Label) mainStage.getScene().lookup("#modelDetails"); // Remove '#' from the ID
        if (modelIDFile != null) {
            modelIDFile.setText("Model ID: " + model.getModelId() + "\nFile Path: " + model.getFilePath());
        }

        // Update the environment parameters list
        ListView<EnvironmentParameter> environmentParamList = (ListView<EnvironmentParameter>) mainStage.getScene().lookup("#eParamList"); 
        if (environmentParamList != null) {
            environmentParamList.getItems().clear(); // Clear existing items
            environmentParamList.getItems().addAll(model.getEnvironmentParameters()); // Add new items from the model
        }

        // Update the dependency parameters list
        ListView<DependancyParameter> dependancyParamList = (ListView<DependancyParameter>) mainStage.getScene().lookup("#dParamList"); 
        if (dependancyParamList != null) {
            dependancyParamList.getItems().clear(); // Clear existing items
            dependancyParamList.getItems().addAll(model.getDependencyParameters()); // Add new items from the model
        }

        // Update the internal parameters list
        ListView<InternalParameter> internalParamList = (ListView<InternalParameter>) mainStage.getScene().lookup("#iParamList"); 
        if (internalParamList != null) {
            internalParamList.getItems().clear(); // Clear existing items
            internalParamList.getItems().addAll(model.getInternalParameters()); // Add new items from the model
        }

        // Update the undefined parameters list
        ListView<UndefinedParameter> undefinedParamList = (ListView<UndefinedParameter>) mainStage.getScene().lookup("#uParamList"); 
        if (undefinedParamList != null) {
            undefinedParamList.getItems().clear(); // Clear existing items
            undefinedParamList.getItems().addAll(model.getUndefinedParameters()); // Add new items from the model
        }
        updateButtonStates(model);
    }
    
    // TODO: re-factor (create file dialog method)
    private void saveModelsToFile(List<Model> models, String filePath) {
        JSONObject root = new JSONObject();
        JSONObject modelsObject = new JSONObject();

        for (Model model : models) {
            JSONObject modelObject = new JSONObject();
            modelObject.put("id", model.getModelId());
            modelObject.put("fileName", model.getFilePath());

            // Parameters object
            JSONObject parametersObject = new JSONObject();

            // Handling dependency parameters
            JSONObject dependencyObject = new JSONObject();
            for (DependancyParameter dep : model.getDependencyParameters()) {
                JSONObject depObj = new JSONObject();
                depObj.put("name", dep.getName());
                depObj.put("modelId", dep.getModelID());
                depObj.put("property", dep.getDefinition());
                dependencyObject.put(dep.getName(), depObj);  // Add dependency parameters to the object
            }
            parametersObject.put("dependency", dependencyObject);

            // Handling environment parameters
            JSONObject environmentObject = new JSONObject();
            for (EnvironmentParameter env : model.getEnvironmentParameters()) {
                JSONObject envObj = new JSONObject();
                envObj.put("name", env.getName());
                envObj.put("type", env.getCalculation());
                envObj.put("dataFile", env.getFilePath());
                environmentObject.put(env.getName(), envObj);  // Add environment parameters
            }
            parametersObject.put("environment", environmentObject);

            // Handling internal parameters (only name should be included)
            JSONObject internalObject = new JSONObject();
            for (InternalParameter internal : model.getInternalParameters()) {
                JSONObject internalObj = new JSONObject();
                internalObj.put("name", internal.getName());  // Only name for internal parameters
                internalObject.put(internal.getName(), internalObj);  // Add internal parameters
            }
            parametersObject.put("internal", internalObject);

            // Add the parameters to the model object
            modelObject.put("parameters", parametersObject);

            // Add model to the modelsObject
            modelsObject.put(model.getModelId(), modelObject);
        }

        // Wrap everything under "models"
        root.put("models", modelsObject);

        // Save the JSON to file
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(root.toString(4));  // Pretty print with indentation
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private VBox createCellLayout(Parameter item) {
        // Parse the item into fields
        String[] fields = item.toString().split(",");
        String f1 = fields.length > 0 ? fields[0] : "";
        String f2 = fields.length > 1 ? fields[1] : "";
        String f3 = fields.length > 2 ? fields[2] : "";

        // Create a VBox for the overall layout
        VBox vbox = new VBox(5);
        // Create HBox for f1 with buttons
        HBox f1Box = createHBoxForF1(f1, item);

        if (!(item instanceof InternalParameter)) {
        	// Create a VBox for f2 and f3 with indentation and bullets
        	VBox subParamsBox = createSubParamsBox(f2, f3);
            // Add the HBox and VBox to the main VBox
            vbox.getChildren().addAll(f1Box, subParamsBox);
        }
        else {
        	vbox.getChildren().add(f1Box);
        }
        return vbox;
    	
    }
    
    private HBox createHBoxForF1(String f1, Parameter item) {
        HBox f1Box = new HBox(10);
        Label f1Label = new Label(f1);
        f1Label.setStyle("-fx-font-weight: bold;"); // Make f1 bold
        f1Label.setStyle(font); // Set the font size to 30px
        Button minusButton = new Button("-");
        Button editButton = new Button("Edit");

        // Add event handlers for the buttons
        minusButton.setOnAction(e -> handleRemoveItem(item));
        editButton.setOnAction(e -> handleEditItem(item));

        f1Box.getChildren().addAll(f1Label, minusButton, editButton);
        return f1Box;
    }

    // Helper method to create VBox for f2 and f3
    private VBox createSubParamsBox(String f2, String f3) {
        VBox subParamsBox = new VBox(5);
        subParamsBox.setPadding(new Insets(0, 0, 0, 20)); // Indent the sub-parameters
        Label f2Label = new Label("• " + f2);
        Label f3Label = new Label("• " + f3);
        f2Label.setStyle(font2); // Set the font size to 30px
        f3Label.setStyle(font2); // Set the font size to 30px
        subParamsBox.getChildren().addAll(f2Label, f3Label);
        return subParamsBox;
    }
}
// TODO task list
/**
 * Highlight models green/red depending on whether all parameters defined
 * Implement adding parameters from drop-down list of undefined parameters
 * Hide/showing undefined parameters section when empty/non-empty
 * Ensure window cannot be made too small to obscure the GUI
 * Do not allow adding of undefined when list empty (remove "+" buttons?)
 * prevent interaction with main window when dialog open
 */