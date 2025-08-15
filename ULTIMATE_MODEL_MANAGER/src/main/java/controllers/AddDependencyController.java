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
import ultimate.Ultimate;

public class AddDependencyController {
	
	@FXML private ChoiceBox<UncategorisedParameter> undefinedParameters; // to list the ucs in the model
	@FXML private ChoiceBox<Model> chooseModel; // choose from list of models in project
	@FXML private TextField definition; // the definition of the dep param
	@FXML private Button saveButton;
	@FXML private Button cancelButton;
	
    private SharedContext sharedContext = SharedContext.getContext();
	private Ultimate ultimate = SharedContext.getUltimateInstance();
    private Project project = SharedContext.getProject();
	
	@FXML
	public void initialize() {
		undefinedParameters.setItems(project.getCurrentModel().getUncategorisedParameters());
		chooseModel.setItems(project.getObservableModels());
		
	    // Set a custom StringConverter to display models using the toName() method
	    chooseModel.setConverter(new javafx.util.StringConverter<Model>() {
	        @Override
	        public String toString(Model model) {
	            return model != null ? model.toName() : "";
	        }

	        @Override
	        public Model fromString(String string) {
	            // This method is not used for ChoiceBox, so it can return null
	            return null;
	        }
	    });
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
