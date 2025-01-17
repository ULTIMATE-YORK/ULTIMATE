package controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import persistent_objects.Model;
import persistent_objects.Property;
import persistent_objects.SharedData;
import persistent_objects.UndefinedParameter;
import utils.Alerter;
import utils.Animations;

public class Properties extends Controller {
	
	private String font = "-fx-font-size: 30px;";

	@FXML private ListView<Property> propertyListView;
	@FXML private VBox propertyVBox;
	@FXML private TextField propertyDialogDefinition;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;
		
	private SharedData context;
	
	@FXML
	private void initialize() {
	    // Fetch shared data from the SharedContext
        context = SharedData.getInstance();
        registerController();
        setUpPropertyListView();
	}
	
	private void setUpPropertyListView() {
        propertyListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Property item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label label = new Label(item.getDefinition()); // Display the model ID
                    label.setStyle(font); // Apply font styling
                    setGraphic(label); // Set the label as the cell's graphic
                    setText(null); // Clear any text (not needed with graphic)
                }
            }
        });
        
        propertyVBox.setVisible(false);
        
        // Manually manage visibility with animation
        propertyListView.getItems().addListener((ListChangeListener<? super Property>) change -> {
            if (!propertyListView.getItems().isEmpty()) {
                // Animate VBox expansion when there are items
            	propertyVBox.setVisible(true); // Ensure it's visible for animation
                Animations.animateVBoxExpansion(propertyVBox, context.getModelListController().modelListVBox, 50.0, 50.0, "vertical");
            } else {
                // Animate VBox shrinking and hide after animation
                Animations.animateVBoxExpansion(context.getModelListController().modelListVBox, propertyVBox, 100.0, 0.0, "vertical");

                // Delay hiding and unmanaging until after the animation completes
                Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(500), event -> {
                    	propertyVBox.setVisible(false);
                    })
                );
                timeline.play();
            }
        });

	    // Bind the managed property to the visible property so it doesn't take up space when invisible
	    propertyVBox.managedProperty().bind(propertyVBox.visibleProperty());
	}
	
	@FXML
	private void save() {
		// run validation
		String def = propertyDialogDefinition.getText();
		if (def == "") {
			Alerter.showAlert("ERROR: No Definition Found", "Please enter a definition!");
		}
		else {
			Property prop = new Property(context.getCurrentModel().getModelId(), def);
			context.getCurrentModel().addProperty(prop);
		}
		update(context.getCurrentModel());
		cancel();
	}
	
	@FXML
	private void cancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
	}
	
	public void update(Model currentModel) {
        if (propertyListView != null) {
        	propertyListView.getItems().clear(); // Clear existing items
        	propertyListView.getItems().addAll(currentModel.getProperties()); // Add new items from the model
        }
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerController() {
		context.setPropertiesController(this);
	}
}
