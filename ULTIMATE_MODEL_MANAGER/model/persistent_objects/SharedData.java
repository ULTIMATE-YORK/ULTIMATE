package persistent_objects;

import controllers.Model_List;
import controllers.Parameters;
import controllers.Properties;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public class SharedData {
    private static final SharedData instance = new SharedData(); // Singleton instance

    private ObservableList<Model> models = FXCollections.observableArrayList();
    private Stage mainStage;
    private Model currentModel;
    private Parameters parametersController;
    private Properties propertiesController;
    private Model_List modelController;

    private SharedData() {}

    public static SharedData getInstance() {
        return instance;
    }

    public ObservableList<Model> getModels() {
        return models;
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }
    
	public Model getCurrentModel() {
		return currentModel;
	}
	
	public void setCurrentModel(Model currentModel) {
		this.currentModel = currentModel;
	}
	
    public Parameters getParametersController() {
        return parametersController;
    }

    public void setParametersController(Parameters parametersController) {
        this.parametersController = parametersController;
    }
    
    public Properties getPropertiesController() {
        return propertiesController;
    }

    public void setPropertiesController(Properties propertiesController) {
        this.propertiesController = propertiesController;
    }
    
    public Model_List getModelController() {
        return modelController;
    }

    public void setModelController(Model_List modelController) {
        this.modelController = modelController;
    }
}
