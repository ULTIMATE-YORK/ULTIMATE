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
				chooseText.setEditable(true);
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
				// show read-only file name field and picker button
				chooseText.setEditable(false);
				chooseText.setVisible(true);
				chooseText.setManaged(true);
				chooseText.setText("");
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
		if (dataFile != null) {
			chooseText.setText(dataFile);
		}
	}

	@FXML
	private void saveEParam() {
		String type = chooseType.getValue();
		String value = chooseText.getText();

		if (type.toLowerCase().equals("fixed")) {
			try {
				project.getTargetModel().removeExternalParameter(ep);
				project.getTargetModel()
						.addExternalParameter(
								new FixedExternalParameter(ep.getNameInModel(), value,
										project.getTargetModel().getModelId()));
				closeDialog();
			} catch (NumberFormatException e) {
				Platform.runLater(() -> Alerter.showErrorAlert("Invalid File type", e.getMessage()));
				closeDialog();
			}

		} else if (type.toLowerCase().equals("ranged")) {
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
				project.getTargetModel().removeExternalParameter(ep);
				project.getTargetModel()
						.addExternalParameter(new RangedExternalParameter(ep.getNameInModel(), rangedValues,
								project.getTargetModel().getModelId()));
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
				project.getTargetModel().removeExternalParameter(ep);
				project.getTargetModel()
						.addExternalParameter(
								new LearnedExternalParameter(ep.getNameInModel(), type.toLowerCase(), dataFile,
										project.getTargetModel().getModelId()));
				closeDialog();
			} catch (NumberFormatException e) {
				Platform.runLater(() -> Alerter.showErrorAlert("Invalid File type", e.getMessage()));
				closeDialog();
			} catch (IOException e) {
				Platform.runLater(() -> Alerter.showErrorAlert("Could not open file", e.getMessage()));
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
		if (ep instanceof FixedExternalParameter) {
			chooseType.setValue("Fixed");
			chooseText.setText(((FixedExternalParameter) ep).getValue());
		} else if (ep instanceof RangedExternalParameter) {
			chooseType.setValue("Ranged");
			java.util.ArrayList<String> options = ((RangedExternalParameter) ep).getValueOptions();
			if (!options.isEmpty()) {
				rangeMin.setText(options.get(0));
				rangeMax.setText(options.get(options.size() - 1));
				if (options.size() > 1) {
					java.math.BigDecimal step = new java.math.BigDecimal(options.get(1))
							.subtract(new java.math.BigDecimal(options.get(0)));
					rangeStep.setText(step.toPlainString());
				}
			}
		} else if (ep instanceof LearnedExternalParameter) {
			LearnedExternalParameter lep = (LearnedExternalParameter) ep;
			for (String item : chooseType.getItems()) {
				if (item.equalsIgnoreCase(lep.getType())) {
					chooseType.setValue(item);
					break;
				}
			}
			dataFile = lep.getValueSource();
			chooseText.setText(dataFile != null ? dataFile : "");
		}
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
