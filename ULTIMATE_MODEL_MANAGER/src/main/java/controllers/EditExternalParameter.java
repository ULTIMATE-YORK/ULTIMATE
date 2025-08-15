package controllers;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import parameters.ExternalParameter;
import parameters.FixedExternalParameter;
import parameters.LearnedExternalParameter;
import parameters.RangedExternalParameter;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public class EditExternalParameter {

	@FXML
	private Label valueLabel;
	@FXML
	private ChoiceBox<String> chooseType;
	@FXML
	private Label rangeLabel;
	@FXML
	private TextField rangeMin;
	@FXML
	private TextField rangeMax;
	@FXML
	private TextField rangeStep;
	@FXML
	private TextField chooseText;
	@FXML
	private Button chooseButton;
	@FXML
	private Button saveButton;
	@FXML
	private Button cancelButton;

	private String dataFile = null;

	private Project project = SharedContext.getProject();

	private ExternalParameter ep;

	private static final Logger logger = LoggerFactory.getLogger(EditExternalParameter.class);

	private boolean ranged = false;

	@FXML
	private void initialize() {
		chooseType.getItems().addAll("Fixed", "Mean", "Mean-Rate", "Bayes", "Bayes-Rate", "Ranged");

		valueLabel.setManaged(false);
		valueLabel.setVisible(false);
		chooseText.setVisible(false);
		chooseText.setManaged(false); // Ensures it doesn't take up space when hidden
		chooseButton.setVisible(false);
		chooseButton.setManaged(false);

		// Add listener to handle visibility
		chooseType.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal.equals("Fixed")) {
				// show text area and hide button
				chooseText.setVisible(true);
				chooseText.setManaged(true);
				chooseButton.setVisible(false);
				chooseButton.setManaged(false);
				valueLabel.setManaged(true);
				valueLabel.setVisible(true);
				// remove ranged if there
				ranged = false;
				rangeLabel.setVisible(false);
				rangeLabel.setManaged(false);
				rangeMin.setVisible(false);
				rangeMin.setManaged(false);
				rangeMax.setVisible(false);
				rangeMax.setManaged(false);
				rangeStep.setVisible(false);
				rangeStep.setManaged(false);
			} else if (newVal.equals("Ranged")) {
				ranged = true;
				rangeLabel.setVisible(true);
				rangeLabel.setManaged(true);
				rangeMin.setVisible(true);
				rangeMin.setManaged(true);
				rangeMax.setVisible(true);
				rangeMax.setManaged(true);
				rangeStep.setVisible(true);
				rangeStep.setManaged(true);
				chooseButton.setVisible(false);
				chooseButton.setManaged(false);
				valueLabel.setVisible(false);
				valueLabel.setManaged(false);
				chooseText.setVisible(false);
				chooseText.setManaged(false);
			} else {
				// show button
				chooseText.setVisible(false);
				chooseText.setManaged(false);
				chooseButton.setVisible(true);
				chooseButton.setManaged(true);
				valueLabel.setManaged(true);
				valueLabel.setVisible(true);
				ranged = false;
				rangeLabel.setVisible(false);
				rangeLabel.setManaged(false);
				rangeMin.setVisible(false);
				rangeMin.setManaged(false);
				rangeMax.setVisible(false);
				rangeMax.setManaged(false);
				rangeStep.setVisible(false);
				rangeStep.setManaged(false);
			}

		});
	}

	@FXML
	private void chooseDataFile() {
		dataFile = openDataFileDialog(SharedContext.getMainStage());
	}

	@FXML
	private void saveEParam() {
		String type = chooseType.getValue();
		String value = chooseText.getText();

		if (type.toLowerCase() == "fixed") {
			try {
				project.getCurrentModel().removeExternalParameter(ep);
				project.getCurrentModel()
						.addExternalParameter(new FixedExternalParameter(ep.getName(), value));
				closeDialog();
			} catch (NumberFormatException e) {
				Platform.runLater(() -> Alerter.showErrorAlert("Invalid File type", e.getMessage()));
				closeDialog();
			}

		} else if (type.toLowerCase() == "ranged") {
			ArrayList<String> rangedValues = new ArrayList<String>();
			try {
				Double min = Double.parseDouble(rangeMin.getText());
				Double max = Double.parseDouble(rangeMax.getText());
				Double step = Double.parseDouble(rangeStep.getText());
				if (max < min) {
					Alerter.showErrorAlert("Invalid Input!", "The minimum value must be less than the maximum value.");
					return;
				}
				if (step <= 0) {
					Alerter.showErrorAlert("Invalid Range!", "The step value must be greater than 0.");
					return;
				}

				for (BigDecimal v = BigDecimal.valueOf(min); v.compareTo(
						BigDecimal.valueOf(max)) <= 0; v = v.add(BigDecimal.valueOf(step))) {
					rangedValues.add(v.setScale(3, RoundingMode.HALF_UP).toString());
				}
			} catch (NumberFormatException e) {
				Alerter.showErrorAlert("Invalid Range", "The range values must be numbers.");
				return;
			}

			try {
				project.getCurrentModel().removeExternalParameter(ep);
				project.getCurrentModel()
						.addExternalParameter(new RangedExternalParameter(ep.getName(), rangedValues));
				closeDialog();
			} catch (IOException e) {
				closeDialog();
			} catch (NumberFormatException e) {
				Platform.runLater(() -> Alerter.showErrorAlert("Invalid File type", e.getMessage()));
				closeDialog();
			}
		}

		else if (LearnedExternalParameter.LEARNED_PARAMETER_TYPE_OPTIONS.contains(type.toLowerCase())) {
			try {
				project.getCurrentModel().removeExternalParameter(ep);
				project.getCurrentModel()
						.addExternalParameter(new FixedExternalParameter(ep.getName(), value));
				closeDialog();
			} catch (NumberFormatException e) {
				Platform.runLater(() -> Alerter.showErrorAlert("Invalid File type", e.getMessage()));
				closeDialog();
			}

		}
	}

	@FXML
	private void closeDialog() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}

	public void setEP(ExternalParameter ep) {
		this.ep = ep;
	}

	private String openDataFileDialog(Stage stage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose a Data File");
		// Set the initial directory (change the path to your specific directory)
		File initialDir = null;
		try {
			initialDir = new File(project.getDirectoryPath());
		} catch (Exception e) {

		}
		if (initialDir != null) {
			fileChooser.setInitialDirectory(initialDir);
		}
		fileChooser.getExtensionFilters()
				.add(new FileChooser.ExtensionFilter("Data files (*.dat, *txt)", "*.dat", "*.txt"));
		File selectedFile = fileChooser.showOpenDialog(stage);
		// Check if the file is in the desired directory
		if (selectedFile != null) {
			try {
				// Get the canonical paths to compare accurately (handles symbolic links, etc.)
				String selectedPath = selectedFile.getCanonicalPath();
				String allowedDirPath = initialDir.getCanonicalPath();

				if (!selectedPath.startsWith(allowedDirPath)) {
					// The file is not in the allowed directory
					Alerter.showErrorAlert("Data File must be in project directory!",
							"Choose a file from the same directory as the project");
					return null;
				}
			} catch (IOException e) {
				logger.error(e.getMessage());
				return null;
			}
			return selectedFile.getName();
		}
		return null;
	}

}
