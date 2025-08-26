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
		
	@FXML private ChoiceBox<String> type;
	@FXML private TextField min;
	@FXML private TextField max;
	@FXML private TextField interval;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;

    private Project project = SharedContext.getProject();
	
	private InternalParameter ip;
	
	@FXML
	public void initialize() {
		type.getItems().addAll("int range","int set","double range", "bool", "distribution");
	}
	
	@FXML
	private void saveIParam() {
		String typeValue = type.getValue();
        String min = this.min.getText();
        String max = this.max.getText();
        Double interval = Double.parseDouble(this.interval.getText());
        if (typeValue == null || min == null || max == null || interval == null) {
        	Alerter.showErrorAlert("Invalid Parameter", "Please define all parameters!");
        	return;
        }
        else {
        	InternalParameter internalParam = new InternalParameter(ip.getName(), min, max);
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
