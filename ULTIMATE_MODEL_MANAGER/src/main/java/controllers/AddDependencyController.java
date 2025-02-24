package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Model;
import parameters.DependencyParameter;
import parameters.UncategorisedParameter;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public class AddDependencyController {
	
	@FXML private ChoiceBox<UncategorisedParameter> undefinedParameters; // to list the ucs in the model
	@FXML private ChoiceBox<Model> chooseModel; // choose from list of models in project
	@FXML private TextField definition; // the definition of the dep param
	@FXML private Button saveButton;
	@FXML private Button cancelButton;
	
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();
	
	@FXML
	public void initialize() {
		undefinedParameters.setItems(project.getCurrentModel().getUncategorisedParameters());
		chooseModel.setItems(project.getObservableModels());
	}
	
	@FXML
	private void saveDParam() {
		UncategorisedParameter name = undefinedParameters.getValue();
		Model model = chooseModel.getValue();
		String def = definition.getText();
		// FIXME: add check that definition is valid
		if (name == null || model == null || def.equals("")) {
			Alerter.showErrorAlert("Invalid Parameter", "Please define all parameters!");
			return;
		}
		else {
			DependencyParameter depParam = new DependencyParameter(name.toString(), model, def);
			project.getCurrentModel().addDependencyParameter(depParam);
			project.getCurrentModel().removeUncategorisedParameter(undefinedParameters.getValue());
			closeDialog();
		}
	}
	
	@FXML
	private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
	}
}
