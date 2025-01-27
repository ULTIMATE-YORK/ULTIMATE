package model.persistent_objects;

import java.util.ArrayList;

import gui.controllers.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public class SharedData {
    private static final SharedData instance = new SharedData(); // Singleton instance

    private ObservableList<Model> models = FXCollections.observableArrayList();
    private Stage mainStage;
    private Model currentModel;
    private Property currentProperty;
    private ParametersController parametersController;
    private PropertiesController propertiesController;
    private ModelListController modelController;
    private MenuBarController menuBarController;
    private Tab1Controller tab1Controller;
    private Tab2Controller tab2Controller;
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
	
    public ParametersController getParametersController() {
        return parametersController;
    }

    public void setParametersController(ParametersController parametersController) {
        this.parametersController = parametersController;
        allControllers.add(parametersController);
    }
    
    public PropertiesController getPropertiesController() {
        return propertiesController;
    }

    public void setPropertiesController(PropertiesController propertiesController) {
        this.propertiesController = propertiesController;
        allControllers.add(propertiesController);
    }
    
    public ModelListController getModelListController() {
        return modelController;
    }

    public void setModelListController(ModelListController modelController) {
        this.modelController = modelController;
        allControllers.add(modelController);
    }
    
    public void setMenuBarController(MenuBarController menuBarController) {
    	this.menuBarController = menuBarController;
    	allControllers.add(menuBarController);
    }
    
    public MenuBarController getMenuBarController() {
    	return this.menuBarController;
    }
    
    public void setTab1Controller(Tab1Controller tab1Controller) {
    	this.tab1Controller = tab1Controller;
    	allControllers.add(tab1Controller);
    }
    
    public Tab1Controller getTab1Controller() {
    	return this.tab1Controller;
    }
    
    public void setTab2Controller(Tab2Controller tab2Controller) {
    	this.tab2Controller = tab2Controller;
    	allControllers.add(tab2Controller);
    }
    
    public Tab2Controller getTab2Controller() {
    	return this.tab2Controller;
    }
    
    public <T extends Controller> void registerController(T controller) {
    	
    	allControllers.add(controller);
    	
    	if (controller instanceof ModelListController) {
    		this.modelController = (ModelListController) controller;
    	}
    	else if (controller instanceof MenuBarController) {
    		this.menuBarController = (MenuBarController) controller;
    	}
    	else if (controller instanceof ParametersController) {
    		this.parametersController = (ParametersController) controller;
    	}
    	else if (controller instanceof PropertiesController) {
    		this.propertiesController = (PropertiesController) controller;
    	}
    	else if (controller instanceof Tab1Controller) {
    		this.tab1Controller = (Tab1Controller) controller;
    	}    	
    	else if (controller instanceof Tab2Controller) {
    		this.tab2Controller = (Tab2Controller) controller;
    	}
    }
    
    public void update() {
    	Platform.runLater( () -> {
	    	for (Controller controller : allControllers) {
	    		controller.update();
	    	}
    	});
    }
}
