package controllers;

import java.io.File;
import java.io.IOException;

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
import parameters.InternalParameter;
import parameters.UncategorisedParameter;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public class EditInternalParameter {

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


	private SharedContext sharedContext = SharedContext.getInstance();
	private Project project = sharedContext.getProject();

	private InternalParameter iParam;

	private static final Logger logger = LoggerFactory.getLogger(EditInternalParameter.class);

//	@FXML
//	private void initialize() {
//
//	}
	
	public void setInternalParameter(InternalParameter iParam) {
		this.iParam = iParam;
		minimumField.setText(iParam.getMinValue());
		maximumField.setText(iParam.getMaxValue());
	}

	@FXML
	private void saveIParam() {
		String parameterName = iParam.getName();
		String minValue = "";
		String maxValue = "";

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

		if (iParam == null || minValue == null || maxValue == null) {
			Alerter.showErrorAlert("Invalid Parameter", "Please define all parameters!");
			return;
		} else {
			try {
				InternalParameter iParam = new InternalParameter(parameterName.toString(), minValue, maxValue);
				project.getCurrentModel().removeInternalParameter(iParam);
				project.getCurrentModel().addInternalParameter(iParam);
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
