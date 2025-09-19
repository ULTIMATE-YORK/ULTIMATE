package controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import model.Model;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;
import utils.DialogOpener;
import utils.Font;

public class ModelController {

	@FXML
	private Button addButton;
	@FXML
	private Button openButton;
	@FXML
	private Button removeButton;
//	@FXML
//	private Button upButton;
//	@FXML
//	private Button downButton;
	@FXML
	private ListView<Model> modelListView;

	private Project project = SharedContext.getProject();

	@FXML
	public void initialize() {
		// Bind the ListView to the project's observable models
		modelListView.setItems(project.getObservableModels());
		setUpModelListView();
		scrollDown();
		
		if (!Desktop.isDesktopSupported()) {
			((Pane)openButton.getParent()).getChildren().remove(openButton);
		}

		
		SimpleBooleanProperty noneSelected = new SimpleBooleanProperty(modelListView.getSelectionModel().getSelectedItem() == null); 
		openButton.disableProperty().bind(noneSelected);
		removeButton.disableProperty().bind(noneSelected);
	}
	

	@FXML
	public void openModel() {
		Model selectedModel = modelListView.getSelectionModel().getSelectedItem();
//		System.out.println(String.format("%s %s %s", selectedModel.getModelId(), selectedModel.getFilePath(), Desktop.isDesktopSupported()));
		if (selectedModel != null && selectedModel.getFilePath() != null && !selectedModel.getFilePath().equals("") && Desktop.isDesktopSupported()) { 
			Task<Void> task = new Task<>() {
			@Override				
			public Void call() throws IOException {           
               Desktop.getDesktop().open(new File(selectedModel.getFilePath()));
               return null;
				};     
	       };
		task.setOnFailed(event->{
			Throwable e = task.getException();
			e.printStackTrace();
			Platform.runLater(()->{
           	   Alerter.showErrorAlert("Could Not Open File", "Could not open the model file at " + selectedModel.getFilePath());
			});
		});
		
		new Thread(task).start();
		
		};
	}
	
	
	@FXML
	public void addModel() {
		String filePath = DialogOpener.openPrismFileDialog(SharedContext.getMainStage());

		if (filePath == null) {
			// User cancelled file selection; no need to proceed
			return;
		}

		// Run model creation in a background thread
		Task<Model> task = new Task<>() {
			@Override
			protected Model call() throws IOException {
				return new Model(filePath); // File processing happens in the background
			}
		};

		// Handle success (update UI safely on JavaFX thread)
		task.setOnSucceeded(event -> {
			Model newModel = task.getValue();
			newModel.addUncategorisedParametersFromFile();
			project.addModel(newModel);
			// System.out.println("Model added successfully: " + newModel);
			if (project.getDirectoryPath() == null) {
				Path file = Paths.get(filePath);
				Path parent = file.getParent();
				project.setDirectory(parent.toString());
			}
		});

		// Handle failure (show error alert)
		task.setOnFailed(event -> {
			Throwable error = task.getException();
			Alerter.showErrorAlert("Model Not Created!", "Error loading file: " + filePath + "\n" + error.getMessage());
		});

		// Start background task
		new Thread(task).start();
	}

	@FXML
	public void scrollUp() {
		int selectedIndex = modelListView.getSelectionModel().getSelectedIndex();
		if (selectedIndex > 0) {
			// Select the item just above the current one
			modelListView.getSelectionModel().select(selectedIndex - 1);
		}
	}

	@FXML
	public void scrollDown() {
		int selectedIndex = modelListView.getSelectionModel().getSelectedIndex();
		if (selectedIndex < modelListView.getItems().size() - 1) {
			// Select the item just below the current one
			modelListView.getSelectionModel().select(selectedIndex + 1);
		}
	}

	@FXML
	void removeModel() {
		Model selectedModel = modelListView.getSelectionModel().getSelectedItem();
		if (selectedModel != null) {
			Boolean userConfirmsDelete = Alerter.showConfirmationAlert("Confirm Model Removal", String.format(
					"Are you sure you want to remove model '%s' from the project?", selectedModel.getModelId()));
			if (userConfirmsDelete) {
				project.removeModel(selectedModel); // Remove the selected model
			}
		}
	}

	/**
	 * Configures the model list view, including its layout, behavior, and event
	 * handling.
	 */
	private void setUpModelListView() {

		// When a new model is selected, the property is updated and the current model
		// is set
		modelListView.getSelectionModel().selectedItemProperty().addListener((obs, oldModel, newModel) -> {
			if (newModel != null) {
				project.setCurrentModel(newModel);
			}
		});

		// Customise the appearance of each item in the list view
		modelListView.setCellFactory(param -> new ListCell<>() {
			@Override
			protected void updateItem(Model item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
					setText(null);
				} else {
					Label label = new Label(item.getModelId()); // Display the model ID
					label.setStyle(Font.MODEL_LIST_FONT); // Apply font styling
					setGraphic(label); // Set the label as the cell's graphic
					setText(null); // Clear any text (not needed with graphic)
				}
			}
		});

		// TODO: make this OS independent
		// Add a key event handler to allow deletion of models using the Backspace key
		// modelListView.setOnKeyPressed(event -> {
		// if (event.getCode() == KeyCode.BACK_SPACE) {
		// Model selectedModel = modelListView.getSelectionModel().getSelectedItem();
		// if (selectedModel != null) {
		// project.removeModel(selectedModel); // Remove the selected model
		// }
		// }
		// });
	}
}
