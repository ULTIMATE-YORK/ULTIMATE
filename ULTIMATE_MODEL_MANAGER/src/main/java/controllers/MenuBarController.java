package controllers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import project.Project;
import sharedContext.SharedContext;
import ultimate.Ultimate;
import utils.Alerter;
import utils.DialogOpener;

public class MenuBarController {
	
	@FXML private MenuItem loadButton;
	@FXML private MenuItem saveButton;
	@FXML private MenuItem saveAsButton;
	@FXML private MenuItem quitButton;
	@FXML private MenuItem choosePrism;
	@FXML private MenuItem chooseStorm;
	
    private Project project = SharedContext.getProject();
	
	private static final Logger logger = LoggerFactory.getLogger(MenuBarController.class);

    @FXML
	private void initialize() {
        
    	// Tick icon (using a checkmark image or symbol)
        ImageView prismtickIcon = new ImageView(new Image("/images/green_tick.png"));  // Example image
        prismtickIcon.setFitWidth(15);
        prismtickIcon.setFitHeight(15);
        
        
    	// Tick icon (using a checkmark image or symbol)
        ImageView stormtickIcon = new ImageView(new Image("/images/green_tick.png"));  // Example image
        stormtickIcon.setFitWidth(15);
        stormtickIcon.setFitHeight(15);

        // Dynamically add/remove tick based on the property
        choosePrism.graphicProperty().bind(Bindings.when(project.chosenPMCProperty().isEqualTo("PRISM"))
                .then(prismtickIcon)
                .otherwise((ImageView) null));
        
        // Dynamically add/remove tick based on the property
        chooseStorm.graphicProperty().bind(Bindings.when(project.chosenPMCProperty().isEqualTo("STORM"))
                .then(stormtickIcon)
                .otherwise((ImageView) null));
	}
	
	@FXML
	private void load() throws IOException {
		String file = DialogOpener.openUltimateFileDialog(SharedContext.getMainStage());
		if (file == null) {
			return;
		}
		if (quit()) {
			// TODO: pull this out into method
			Stage newMainStage = new Stage();
			SharedContext.setMainStage(newMainStage);
			Project project = new Project(file);
			SharedContext.setProject(project);
			Ultimate ultimate = new Ultimate();
			SharedContext.setUltimate(ultimate);
	        // Load the FXML file and initialize its associated controller
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_view.fxml")); // Specifies the FXML path
	        
	        // Load the root layout from the FXML file (in this case, a GridPane layout)
	        GridPane root = loader.load(); // The root layout is defined in main_view.fxml

	        // Create a Scene using the root layout and set its dimensions
	        newMainStage.setScene(new Scene(root, 1500, 1000)); // Scene dimensions: 1500x1000 pixels

	        // Set the title of the primary stage (window)
	        //stage.setTitle("Ultimate Stochastic World Model Manager: UNTITLED"); // Customize the window title as needed

	        // Set minimum dimensions for the primary stage
	        newMainStage.setMinWidth(1000); // Ensure the stage cannot be resized smaller than 800px in width
	        newMainStage.setMinHeight(800); // Ensure the stage cannot be resized smaller than 600px in height

	        // Display the stage (window) to the user
	        newMainStage.show(); // Makes the primary stage visible
	        
	        if (!project.isConfigured()) {
	            newMainStage.close();
	        }
		}
	}
	
	@FXML
	private void save() {
		if (project.getSaveLocation() == null) {
			saveAs();
		}
		else {
			project.save();
		}
	}
	
	@FXML private void saveAs() {
		String location = DialogOpener.openUltimateSaveDialog(SharedContext.getMainStage());
		// deal with cancel neatly
		if (location == null) {
			return;
		}
		project.setSaveLocation(location);
		project.save(location);
	}
	
	@FXML
	private boolean quit() {
		if (!project.isBlank()) {
			boolean q = Alerter.showConfirmationAlert("Project Not Saved!", "Are you sure you want to quit without saving?");
			if (q) {
				SharedContext.getMainStage().close();
				return true;
			}
			else {
				return false;
			}
		}
		SharedContext.getMainStage().close();
		return true;
	}
	
	@FXML
	private void setPrismPMC() {
		logger.info("Setting PMC to PRISM...");
		project.setChosenPMC("PRISM");
	}
	
	@FXML
	private void setStormPMC() {
		logger.info("Setting PMC to STORM...");
		project.setChosenPMC("STORM");
	}

}
