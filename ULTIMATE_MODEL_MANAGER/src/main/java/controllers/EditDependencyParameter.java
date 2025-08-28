package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Model;
import parameters.DependencyParameter;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public class EditDependencyParameter {
	
	@FXML private ChoiceBox<Model> chooseModel;
	@FXML private TextField definition;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;
	
    private Project project = SharedContext.getProject();
    
    private DependencyParameter dp;
	
	@FXML
	private void initialize() {
		chooseModel.setItems(project.getObservableModels());
	}
	
	@FXML
	private void save() {
		Model model = chooseModel.getValue();
		String def = definition.getText();
		// FIXME: add check that definition is valid
		if (model == null || def.equals("")) {
			Alerter.showErrorAlert("Invalid Parameter", "Please define all parameters!");
			return;
		}
		else {
			DependencyParameter depParam = new DependencyParameter(dp.getNameInModel(), model, def,project.getTargetModel().getModelId());
			project.getTargetModel().addDependencyParameter(depParam);
			project.getTargetModel().removeDependencyParameter(dp);
			project.refresh();
			closeDialog();
		}
	}

	@FXML
	private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
	}
	
	public void setDP(DependencyParameter dp) {
		this.dp = dp;
	}
}
