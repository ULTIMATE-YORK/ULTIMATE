package gui.controllers;

import model.persistent_objects.*;
import utils.Animations;
import utils.DialogLoader;
import utils.ModelUtils;

import java.io.File;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ParametersController extends Controller {
	
	@FXML private Label modelDetails;
	@FXML private Button addEnvironmentParamButton;
	@FXML private Button addDependencyParamButton;
    @FXML private Button addInternalParamButton;
    @FXML private ListView<EnvironmentParameter> eParamList;
    @FXML private ListView<DependencyParameter> dParamList;
    @FXML private ListView<InternalParameter> iParamList;
    @FXML private ListView<UndefinedParameter> uParamList;
    
    // VBox to control displaying of undefined parameters
    @FXML private VBox undefinedParametersVBox;
    @FXML private VBox parameterDialogs;
    @FXML private GridPane parameterGridPane;
    
    // for pop-ups
    @FXML private ChoiceBox<UndefinedParameter> undefinedParameters; // common for all parameters
    @FXML private TextField nameField; // Common field for all parameters
    @FXML private Button saveButton; //common for all parameters
    @FXML private Button cancelButton; //common for all parameters
    
    @FXML private Button chooseDataFile; // Specific to EnvironmentParameters
    @FXML private Label dataFile; // Specific to EnvironmentParameters
    @FXML private ChoiceBox<String> calculations; // specific to environment parameters
    
    @FXML private ChoiceBox<String> chooseModelID; // Specific to DependencyParameters\
    @FXML private TextField definition; // Specific to DependencyParameters
	
	private String font = "-fx-font-size: 18px;";
	private String font2 = "-fx-font-size: 12px;";
	
	private SharedData context;
    
    @FXML
    private void initialize() {
	    // Fetch shared data from the SharedContext
        context = SharedData.getInstance();
        registerController();
        // set up items and bindings
        setUpParamLists();
        setUpBindings();
    }
    
    private void setUpBindings() {
        // Manually manage visibility with animation
		// TODO pull out into seperate method

        uParamList.getItems().addListener((ListChangeListener<? super UndefinedParameter>) change -> {
            if (!uParamList.getItems().isEmpty()) {
                // Animate VBox expansion when there are items
                undefinedParametersVBox.setVisible(true); // Ensure it's visible for animation
                Animations.animateVBoxExpansion(undefinedParametersVBox, parameterDialogs, 50.0, 50.0, "horizontal");
            } else {
                // Animate VBox shrinking and hide after animation
                Animations.animateVBoxExpansion(parameterDialogs, undefinedParametersVBox, 100.0, 0.0, "horizontal");

                // Delay hiding and unmanaging until after the animation completes
                Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(500), event -> {
                        undefinedParametersVBox.setVisible(false);
                    })
                );
                timeline.play();
            }
        });

        // Ensure managed property is always in sync with visibility
        undefinedParametersVBox.managedProperty().bind(undefinedParametersVBox.visibleProperty());
        // bind the buttons visibility to whether the undefinedParametersVBox is managed
        addEnvironmentParamButton.visibleProperty().bind(undefinedParametersVBox.managedProperty());
        addDependencyParamButton.visibleProperty().bind(undefinedParametersVBox.managedProperty());
        addInternalParamButton.visibleProperty().bind(undefinedParametersVBox.managedProperty());
    }
    
	private void setUpParamLists() {
		// Behaviour and bindings for parameter lists
		setUpListViewCellFactory(eParamList);
		setUpListViewCellFactory(dParamList);
		setUpListViewCellFactory(iParamList);
		setUpListViewCellFactory(uParamList);
	}
    
	// FIXME shouldn't open if no current model 
    @FXML
    private void handleEParam() {
    	// load the pop up dialog for adding an environment parameter
    	loadAddParamDialog("Environment");	
    }
    
    @FXML
    private void handleDParam() {
    	loadAddParamDialog("Dependency");
    }
    
    @FXML
    private void handleIParam() {
    	loadAddParamDialog("Internal");
    }
    
    // This method is called from Model_List.java when the selected model is changed
	public void update(Model currentModel) {
		modelDetails.setText("Model ID: " + currentModel.getModelId() + "\nFile Path: " + currentModel.getFilePath());
        
		// Update the environment parameters list
        if (eParamList != null) {
        	eParamList.getItems().clear(); // Clear existing items
            eParamList.getItems().addAll(currentModel.getEnvironmentParameters()); // Add new items from the model
        }

        if (dParamList != null) {
            dParamList.getItems().clear(); // Clear existing items
            dParamList.getItems().addAll(currentModel.getDependencyParameters()); // Add new items from the model
        }

        if (iParamList != null) {
            iParamList.getItems().clear(); // Clear existing items
            iParamList.getItems().addAll(currentModel.getInternalParameters()); // Add new items from the model
        }

        if (uParamList != null) {
            uParamList.getItems().clear(); // Clear existing items
            uParamList.getItems().addAll(currentModel.getUndefinedParameters()); // Add new items from the model
        }
		
	}
	
	private void loadAddParamDialog(String type) {
		try {
			DialogLoader.load("/dialogs/add_"+type+"_param.fxml", "Add " + type + " Parameter", this);
            switch (type) {
			case "Environment":
				// Set up the dialog for adding an environment parameter
				initEParamDialog();
				break;
            case "Dependency":
            	// Set up the dialog for adding a dependency parameter
            	initDParamDialog();
            	break;
            case "Internal":
            	initIParamDialog();
            	break;
            }

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void loadEditParamDialog(String type, Parameter param) {
		try {
			DialogLoader.load("/dialogs/edit_"+type+"_param.fxml", "Edit " + type + " Parameter", this);
            switch (type) {
			case "Environment":
				// Set up the dialog for adding an environment parameter
				initEditEParamDialog((EnvironmentParameter) param);
				break;
            case "Dependency":
            	// Set up the dialog for adding a dependency parameter
            	initEditDParamDialog((DependencyParameter) param);
            	break;
            case "Internal":
            	initEditIParamDialog((InternalParameter) param);
            	break;
            }

		} catch (Exception e) {
			e.printStackTrace();
		}
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
	            } else if (item instanceof EnvironmentParameter || item instanceof DependencyParameter || item instanceof InternalParameter) {
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
    
    @FXML
    private void chooseEnvironmentDataFile() {
    	// get the current stage
    	Stage owner = (Stage) chooseDataFile.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose data file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Data Files", "*.dat"));
        File file = fileChooser.showOpenDialog(owner);
		if (file != null) {
			// set the value of the label to file path
			dataFile.setText(file.getAbsolutePath());
		};
    }
        
    private void initEParamDialog() {
    	// load the values of undefinedParameters into the choice box
	 	Model currentModel = context.getCurrentModel();
	 	// Populate the ChoiceBox after the FXML is loaded
	 	ObservableList<UndefinedParameter> observableItems = FXCollections.observableArrayList(currentModel.getUndefinedParameters());
	 	undefinedParameters.setItems(observableItems); // Set the list as items for the ChoiceBox
	 	// Set the name field to the selected item in the undefinedParameters
	 	undefinedParameters.setOnAction(e -> nameField.setText(undefinedParameters.getValue().getName()));
        calculations.setItems(FXCollections.observableArrayList("Mean"));
    }
    
    private void initDParamDialog() {
    	// load the values of undefinedParameters into the choice box
	 	Model currentModel = context.getCurrentModel();
	 	// Populate the ChoiceBox after the FXML is loaded
	 	ObservableList<UndefinedParameter> observableItems = FXCollections.observableArrayList(currentModel.getUndefinedParameters());
	 	undefinedParameters.setItems(observableItems); // Set the list as items for the ChoiceBox
	 	// Set the name field to the selected item in the undefinedParameters
	 	undefinedParameters.setOnAction(e -> nameField.setText(undefinedParameters.getValue().getName()));
	 	// set the model id
	 	ObservableList<String> modelIDs = FXCollections.observableArrayList(ModelUtils.getModelIDs());
	 	chooseModelID.setItems(modelIDs);
    }
    
    private void initIParamDialog() {
	 	Model currentModel = context.getCurrentModel();
	 	ObservableList<UndefinedParameter> observableItems = FXCollections.observableArrayList(currentModel.getUndefinedParameters());
	 	undefinedParameters.setItems(observableItems); // Set the list as items for the ChoiceBox
	 	undefinedParameters.setOnAction(e -> nameField.setText(undefinedParameters.getValue().getName()));
    }
    
    @FXML
    private void saveEParam() {
	 	Model currentModel = context.getCurrentModel();
	 	String id = nameField.getText();
	 	String dataFilePath = dataFile.getText();
	 	String calculation = calculations.getValue();
	 	EnvironmentParameter eParam = new EnvironmentParameter(id, dataFilePath, calculation);
	 	currentModel.addEnvironmentParameter(eParam);
	 	currentModel.removeUndefinedParamter(undefinedParameters.getValue());
	 	update(currentModel);
	 	closeDialog();
    }
        
    @FXML
    private void saveDParam() {
	 	Model currentModel = context.getCurrentModel();
	 	String name = nameField.getText();
	 	String modelID = chooseModelID.getValue();
	 	String def = definition.getText();
	 	DependencyParameter dParam = new DependencyParameter(name, modelID, def);
	 	currentModel.addDependencyParameter(dParam);
	 	currentModel.removeUndefinedParamter(undefinedParameters.getValue());
	 	update(currentModel);
	 	closeDialog();
    }
    
    @FXML
    private void saveIParam() {
	 	Model currentModel = context.getCurrentModel();
	 	String id = nameField.getText();
	 	currentModel.addInternalParameter(new InternalParameter(id));
	 	currentModel.removeUndefinedParamter(undefinedParameters.getValue());
	 	update(currentModel);
	 	closeDialog();
    }
    
    @FXML
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void initEditEParamDialog(EnvironmentParameter eparam) {
    	saveButton.setOnAction(e -> saveEditEParam(eparam));
        calculations.setItems(FXCollections.observableArrayList("Mean"));
    	nameField.setText(eparam.getName());
    	dataFile.setText(eparam.getFilePath());
    	calculations.setValue(eparam.getCalculation());
    }
    
	private void initEditDParamDialog(DependencyParameter dparam) {
	 	// set the model id
	 	ObservableList<String> modelIDs = FXCollections.observableArrayList(ModelUtils.getModelIDs());
	 	chooseModelID.setItems(modelIDs);
	 	saveButton.setOnAction(e -> saveEditDParam(dparam));
	 	nameField.setText(dparam.getName());
	 	definition.setText(dparam.getDefinition());
	 	chooseModelID.setValue(dparam.getModelID());
	}
	
	private void initEditIParamDialog(InternalParameter iparam) {
		saveButton.setOnAction(e -> saveEditIParam(iparam));
		nameField.setText(iparam.getName());
	}
	
	private void saveEditEParam(EnvironmentParameter eparam) {
	 	String id = nameField.getText();
	 	String dataFilePath = dataFile.getText();
	 	String calculation = calculations.getValue();
	 	eparam.setName(id);
	 	eparam.setFilePath(dataFilePath);
	 	eparam.setCalculation(calculation);
	 	update(context.getCurrentModel());
	 	closeDialog();
	}
	
	private void saveEditDParam(DependencyParameter dparam) {
		String name = nameField.getText();
		String modelID = chooseModelID.getValue();
		String def = definition.getText();
		dparam.setName(name);
		dparam.setModelID(modelID);
		dparam.setDefinition(def);
		update(context.getCurrentModel());
		closeDialog();
	}
    
	private void saveEditIParam(InternalParameter iparam) {
		String id = nameField.getText();
		iparam.setName(id);
		update(context.getCurrentModel());
		closeDialog();
	}
	
    private void handleRemoveItem(Parameter item) {
	 	Model currentModel = context.getCurrentModel();
		String name = item.getName();
    	if (item instanceof EnvironmentParameter) {
    		currentModel.removeEnvironmentParamter((EnvironmentParameter) item);
    		currentModel.addUndefinedParameter(name);
    	}
		else if (item instanceof DependencyParameter) {
			currentModel.removeDependencyParameter((DependencyParameter) item);
			currentModel.addUndefinedParameter(name);
		} else if (item instanceof InternalParameter) {
			currentModel.removeInternalParameter((InternalParameter) item);
			currentModel.addUndefinedParameter(name);
		}
    	update(currentModel);
    }
    
    private void handleEditItem(Parameter item) {
		if (item instanceof EnvironmentParameter) {
			loadEditParamDialog("Environment", item);
		}
		else if (item instanceof DependencyParameter) {
			loadEditParamDialog("Dependency", item);
		} else {
			loadEditParamDialog("Internal", item);
		}
    }

	@Override
	public void update() {
		update(context.getCurrentModel());
		// make uncategorized params invisible if the uParamList is empty (when a model with no uparams added)
		
		// TODO pull out into seperate method
		if (uParamList.getItems().isEmpty()) {
            // Animate VBox shrinking and hide after animation
            Animations.animateVBoxExpansion(parameterDialogs, undefinedParametersVBox, 100.0, 0.0, "horizontal");

            // Delay hiding and unmanaging until after the animation completes
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(500), event -> {
                    undefinedParametersVBox.setVisible(false);
                })
            );
            timeline.play();
		}
	}

	@Override
	public void registerController() {
		context.registerController(this);	}
}