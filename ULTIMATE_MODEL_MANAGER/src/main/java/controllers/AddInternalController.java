package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import parameters.InternalParameter;
import parameters.UncategorisedParameter;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public class AddInternalController {

	@FXML
	private ChoiceBox<UncategorisedParameter> undefinedParameters;
	@FXML
	private TextField min;
	@FXML
	private TextField max;
	@FXML
	private Button saveButton;
	@FXML
	private Button cancelButton;

	private Project project = SharedContext.getProject();

	@FXML
	public void initialize() {
		undefinedParameters.setItems(project.getTargetModel().getUncategorisedParameters());
	}

	@FXML
	private void saveIParam() {
		UncategorisedParameter name = undefinedParameters.getValue();
		// String typeValue = type.getValue();
		String min = this.min.getText();
		String max = this.max.getText();
		// Double interval = Double.parseDouble(this.interval.getText());
		if (name == null || min == null || max == null) {
			Platform.runLater(() -> {
				Alerter.showErrorAlert("Invalid Parameter", "Please define all parameters!");
				return;
			});
		} else {
			InternalParameter internalParam = new InternalParameter(name.toString(), min, max,
					project.getTargetModel().getModelId() + "-" + name.toString());
			project.getTargetModel().addInternalParameter(internalParam);
			project.getTargetModel().removeUncategorisedParameter(undefinedParameters.getValue());
			closeDialog();
		}
	}

	@FXML
	private void closeDialog() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}

}
