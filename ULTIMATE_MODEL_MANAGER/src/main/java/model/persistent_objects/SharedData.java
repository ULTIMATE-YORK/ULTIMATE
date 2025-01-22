package model.persistent_objects;

import java.util.ArrayList;

import gui.controllers.*;
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
    private Tab2 tab2Controller;
    private String pmcEngine = "PRISM"; // defaults the pmc engine to prism
    private String stormInstallation = "";
    
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
	
	public void setPMCEngine(String engine) {
		if (engine == "PRISM" | engine == "STORM") {
			this.pmcEngine = engine;
		}
	}
	
	public String getStormInstallation() {
		return this.stormInstallation;
	}
	
	public void setStormInstallation(String path) {
		stormInstallation = path+"/storm";
	}
	
	public String getPMCEngine() {
		return this.pmcEngine;
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
    
    public void setTab2Controller(Tab2 tab2Controller) {
    	this.tab2Controller = tab2Controller;
    	allControllers.add(tab2Controller);
    }
    
    public Tab2 getTab2Controller() {
    	return this.tab2Controller;
    }
    
    public <T extends Controller >void registerController(T controller) {
    	
    	allControllers.add(controller);
    	
    	if (controller instanceof Model_List) {
    		this.modelController = (Model_List) controller;
    	}
    	else if (controller instanceof Menu_Bar) {
    		this.menuBarController = (Menu_Bar) controller;
    	}
    	else if (controller instanceof Parameters) {
    		this.parametersController = (Parameters) controller;
    	}
    	else if (controller instanceof Properties) {
    		this.propertiesController = (Properties) controller;
    	}
    	else if (controller instanceof Tab1) {
    		this.tab1Controller = (Tab1) controller;
    	}    	
    	else if (controller instanceof Tab2) {
    		this.tab2Controller = (Tab2) controller;
    	}
    }
    
    public void update() {
    	for (Controller controller : allControllers) {
    		controller.update();
    	}
    }
}
