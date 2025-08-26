package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import parameters.SynthesisObjective;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public class AddSynthesisObjectiveController {

	@FXML
	private TextField definitionTextField;
	@FXML
	private ChoiceBox<String> typeChoiceBox;
	@FXML
	private ChoiceBox<String> minOrMaxChoiceBox;
	@FXML
	private TextField constraintTextField;
	@FXML
	private Label constraintValueLabel;
	@FXML
	private Button saveButton;
	@FXML
	private Button cancelButton;

	private Project project = SharedContext.getProject();

	@FXML
	private void initialize() {
		typeChoiceBox.getItems().addAll("Objective", "Constraint");
		minOrMaxChoiceBox.getItems().addAll("min", "max");
		typeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal.equals("Constraint")) {
				constraintTextField.setVisible(true);
				constraintValueLabel.setVisible(true);
			} else {
				constraintTextField.setVisible(false);
				constraintValueLabel.setVisible(false);
			}
		});
	}

	@FXML
	private void save() {
		String definition = definitionTextField.getText();
		String type = typeChoiceBox.getValue();
		String minOrMax = minOrMaxChoiceBox.getValue();
		Double constraint = null;

		if (type == "Constraint") {
			try {
				constraint = Double.parseDouble(constraintTextField.getText());
			} catch (NumberFormatException e) {
				Platform.runLater(() -> {
					Alerter.showErrorAlert("Format Error", String.format("Could not convert %s to a number."));
				});
				return;
			}
		}

		if (definition.equals("")) {
			Alerter.showErrorAlert("Please define the synthesis objective to save", "");
			return;
		} else {

			if (type == "Constraint") {
				SynthesisObjective so = new SynthesisObjective(
						String.format("%s, %s, %f: %s", type, minOrMax, constraint, definition));
				project.getTargetModel().addSynthesisObjective(so);
			} else if (type == "Objective") {
				SynthesisObjective so = new SynthesisObjective(
						String.format("%s, %s: %s", type, minOrMax, definition));
				project.getTargetModel().addSynthesisObjective(so);
			}

			cancel();
		}
	}

	@FXML
	private void cancel() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}

}
