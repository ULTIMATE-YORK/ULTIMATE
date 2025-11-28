package controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Model;
import project.Project;
import sharedContext.SharedContext;
import ultimate.Ultimate;
import utils.Alerter;
import utils.DialogOpener;

public class MenuBarController {

	@FXML
	private MenuItem newMenuItem;
	@FXML
	private MenuItem loadMenuItem;
	@FXML
	private MenuItem saveMenuItem;
	@FXML
	private MenuItem saveAsMenuItem;
	@FXML
	private MenuItem quitMenuItem;
	@FXML
	private MenuItem choosePrism;
	@FXML
	private MenuItem chooseStorm;
	@FXML
	private MenuItem reloadMenuItem;

	private Project project = SharedContext.getProject();

	private static final Logger logger = LoggerFactory.getLogger(MenuBarController.class);

	@FXML
	private void initialize() {

		// Tick icon (using a checkmark image or symbol)
		ImageView prismtickIcon = new ImageView(new Image("/images/green_tick.png")); // Example image
		prismtickIcon.setFitWidth(15);
		prismtickIcon.setFitHeight(15);

		// Tick icon (using a checkmark image or symbol)
		ImageView stormtickIcon = new ImageView(new Image("/images/green_tick.png")); // Example image
		stormtickIcon.setFitWidth(15);
		stormtickIcon.setFitHeight(15);

		// Dynamically add/remove tick based on the property
		choosePrism.graphicProperty().bind(Bindings.when(project.chosenPMCProperty().isEqualTo("PRISM"))
				.then(prismtickIcon).otherwise((ImageView) null));

		// Dynamically add/remove tick based on the property
		chooseStorm.graphicProperty().bind(Bindings.when(project.chosenPMCProperty().isEqualTo("STORM"))
				.then(stormtickIcon).otherwise((ImageView) null));

		// disable/enable MenuItems
		SimpleBooleanProperty reloadDisabledProperty = new SimpleBooleanProperty(
				project.getDirectoryPath() == null || project.getDirectoryPath().equals(""));
		reloadMenuItem.disableProperty().bind(reloadDisabledProperty);

		// keyboard shortcuts
		KeyCombination reloadCombo = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
		KeyCombination saveCombo = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
		KeyCombination saveAsCombo = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
		KeyCombination loadCombo = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
		KeyCombination newCombo = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
		KeyCombination quitCombo = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);

		reloadMenuItem.setAccelerator(reloadCombo);
		loadMenuItem.setAccelerator(loadCombo);
		saveMenuItem.setAccelerator(saveCombo);
		saveAsMenuItem.setAccelerator(saveAsCombo);
		newMenuItem.setAccelerator(newCombo);
		quitMenuItem.setAccelerator(quitCombo);

	}

	@FXML
	private void newProject() {
		if (handleUnsavedChanges()) {
			SharedContext.reset(new Project());
		}
	}

/*
	@FXML
	private void load() throws IOException {
		String filePath = DialogOpener.openUltimateFileDialog(SharedContext.getMainStage());
		if (filePath == null) {
			return;
		}
		if (confirmQuit()) {
			SharedContext.reset(new Project(filePath));
		}
	}
*/

	@FXML
	private void load() throws IOException {
		if (handleUnsavedChanges()) {
			String filePath = DialogOpener.openUltimateFileDialog(SharedContext.getMainStage());
			if (filePath == null){
				return; // User cancelled file dialog
			}
			SharedContext.reset(new Project(filePath));	
		}
	}

	@FXML
	private void save() {
		if (project.getSaveLocation() == null) {
			saveAs();
		} else {
			project.save();
		}
	}

	@FXML
	private void saveAs() {
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
	private boolean confirmQuit() {
		if (project.isModified()) { // CHANGED FROM isBlank() TO isModified()
			boolean userConfirmsQuit = Alerter.showConfirmationAlert("Project Not Saved!",
					"Are you sure you want to quit without saving?");
			if (userConfirmsQuit) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}
	
	private boolean handleUnsavedChanges(){
		if (project.isModified()){
			String result = Alerter.showSaveBeforeActionAlert("Unsaved Changes",
                "Do you want to save changes to the current project?");
                
            if (result.equals("SAVE")){
				save();
				return true; // Proceed with New/Load
			}else if (result.equals("DONT_SAVE")){
				return true; // Proceed without saving
			}else { // CANCEL
				return false; // Don't proceed
			}
		}
		return true; // No unsaved changes, proceed
	}

	@FXML
	private void quit() {
		if (confirmQuit()) {
			SharedContext.getMainStage().close();
		}
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

	@FXML
	private void reload() throws IOException {
		String filePath = project.getProjectFilePath();
		if (filePath == null) {
			return;
		}
		if (confirmQuit()) {
			SharedContext.reset(new Project(filePath));
		}
	}

	@FXML
	private void openEvoCheckerConfig() {
//		System.out.println(String.format("%s %s %s", selectedModel.getModelId(), selectedModel.getFilePath(), Desktop.isDesktopSupported()));
		if (Desktop.isDesktopSupported()) {
			Task<Void> task = new Task<>() {
				@Override
				public Void call() throws IOException {
					Desktop.getDesktop()
							.open(new File(System.getenv("ULTIMATE_DIR") + "/evochecker_config.properties"));
					return null;
				};
			};
			task.setOnFailed(event -> {
				Throwable e = task.getException();
				e.printStackTrace();
				Platform.runLater(() -> {
					Alerter.showErrorAlert("Could Not Open File", "Could not open the config file at "
							+ System.getenv("ULTIMATE_DIR") + "/evochecker_config.properties");
				});
			});

			new Thread(task).start();

		}
		;
	}

}
