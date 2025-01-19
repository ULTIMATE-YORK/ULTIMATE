package persistent_objects;

import java.util.ArrayList;

import controllers.Controller;
import controllers.Menu_Bar;
import controllers.Model_List;
import controllers.Parameters;
import controllers.Properties;
import controllers.Tab1;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public class SharedData {
    private static final SharedData instance = new SharedData(); // Singleton instance

    private ObservableList<Model> models = FXCollections.observableArrayList();
    private Stage mainStage;
    private Model currentModel;
    private Property currentProperty;
    private Parameters parametersController;
    private Properties propertiesController;
    private Model_List modelController;
    private Menu_Bar menuBarController;
    private Tab1 tab1Controller;
    
    private ArrayList<Controller> allControllers = new ArrayList<Controller>();
    
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
	
	public Property getCurrentProperty() {
		return this.currentProperty;
	}
	
	public void setCurrentProperty(Property prop) {
		this.currentProperty = prop;
	}
	
    public Parameters getParametersController() {
        return parametersController;
    }

    public void setParametersController(Parameters parametersController) {
        this.parametersController = parametersController;
        allControllers.add(parametersController);
    }
    
    public Properties getPropertiesController() {
        return propertiesController;
    }

    public void setPropertiesController(Properties propertiesController) {
        this.propertiesController = propertiesController;
        allControllers.add(propertiesController);
    }
    
    public Model_List getModelListController() {
        return modelController;
    }

    public void setModelListController(Model_List modelController) {
        this.modelController = modelController;
        allControllers.add(modelController);
    }
    
    public void setMenuBarController(Menu_Bar menuBarController) {
    	this.menuBarController = menuBarController;
    	allControllers.add(menuBarController);
    }
    
    public Menu_Bar getMenuBarController() {
    	return this.menuBarController;
    }
    
    public void setTab1Controller(Tab1 tab1Controller) {
    	this.tab1Controller = tab1Controller;
    	allControllers.add(tab1Controller);
    }
    
    public Tab1 getTab1Controller() {
    	return this.tab1Controller;
    }
    
    public void update() {
    	for (Controller controller : allControllers) {
    		controller.update();
    	}
    }
}
