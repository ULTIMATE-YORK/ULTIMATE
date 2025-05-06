package controllers;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import parameters.ExternalParameter;
import parameters.InternalParameter;
import parameters.UncategorisedParameter;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public class AddInternalController {

	// FXML references; buttons, UI elements, etc
//	@FXML
//	private Label minLabel;
//	private Label maxLabel;
	@FXML
	private ChoiceBox<UncategorisedParameter> parameterSelectBox;
	@FXML
	private TextField minimumField;
	@FXML
	private TextField maximumField;
	@FXML
	private Button chooseButton;
	@FXML
	private Button saveButton;
	@FXML
	private Button cancelButton;

	private SharedContext sharedContext = SharedContext.getInstance(); // the model
	private Project project = sharedContext.getProject(); // the project
	private static final Logger logger = LoggerFactory.getLogger(AddInternalController.class); // logging

	@FXML
	private void initialize() {

		parameterSelectBox.setItems(project.getCurrentModel().getUncategorisedParameters());

	}

	@FXML
	private void saveIParam() {
		UncategorisedParameter parameterName = parameterSelectBox.getValue();
		String minValue = "";
		String maxValue = "";

		// to do: let a value be infinity by default
		minValue = minimumField.getText();
		maxValue = maximumField.getText();

		// check it's a number
		try {
			double test_val1 = Double.parseDouble(minValue);
			double test_val2 = Double.parseDouble(maxValue);
		} catch (NumberFormatException e) {
			Alerter.showErrorAlert("Invalid Value", "The value must be a number.");
			return;
		}

		if (parameterName == null || minValue == null || maxValue == null) {
			Alerter.showErrorAlert("Invalid Parameter", "Please define all parameters!");
			return;
		} else {
			try {
				InternalParameter iParam = new InternalParameter(parameterName.toString(), minValue, maxValue);
				project.getCurrentModel().addInternalParameter(iParam);
				project.getCurrentModel().removeUncategorisedParameter(parameterSelectBox.getValue());
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

}
