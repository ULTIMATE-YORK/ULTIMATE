package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import parameters.InternalParameter;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public class EditInternalParameter {

	@FXML
	private TextField min;
	@FXML
	private TextField max;
	@FXML
	private Button saveButton;
	@FXML
	private Button cancelButton;

	private Project project = SharedContext.getProject();

	private InternalParameter ip;

	@FXML
	public void initialize() {

	}

	@FXML
	private void saveIParam() {
		String min = this.min.getText();
		String max = this.max.getText();
		if (min == null || max == null) {
			Alerter.showErrorAlert("Invalid Parameter", "Please define all parameters!");
			return;
		} else {
			InternalParameter internalParam = new InternalParameter(ip.getNameInModel(), min, max,
					ip.getNameInModel());
			project.getTargetModel().addInternalParameter(internalParam);
			project.getTargetModel().removeInternalParameter(ip);
			closeDialog();
		}
	}

	@FXML
	private void closeDialog() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}

	public void setIP(InternalParameter ip) {
		this.ip = ip;
	}
}
