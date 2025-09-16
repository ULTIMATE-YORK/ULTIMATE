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
	private void newProject() {
		SharedContext.reset(new Project());
	}

	@FXML
	private void load() throws IOException {
		String filePath = DialogOpener.openUltimateFileDialog(SharedContext.getMainStage());
		if (filePath == null) {
			return;
		}
		if (quitConfirmed()) {
			SharedContext.reset(new Project(filePath));
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
	
	// TODO: fix this
	@FXML
	private boolean quitConfirmed() {
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
