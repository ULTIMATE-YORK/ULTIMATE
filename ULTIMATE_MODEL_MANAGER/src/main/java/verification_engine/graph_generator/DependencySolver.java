package verification_engine.graph_generator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import headless.Solver;
import model.persistent_objects.DependencyParameter;
import model.persistent_objects.Model;
import model.persistent_objects.SharedData;
import prism.PrismException;
import utils.ModelUtils;
import verification_engine.prism.PrismAPI;

public class DependencySolver {
	
	SharedData context = SharedData.getInstance();
	List<Model> models = context.getModels();
	
	// TODO make available to both APIs
	public double solve(Model model, String property) throws FileNotFoundException, PrismException {
		// Base Case is when a mode has no dependencies
		if (model.getDependencyParameters().isEmpty()) {
			if (context.getPMCEngine() == "PRISM") {
				System.out.println("Verifying Model: " + model.getModelId() + "\nProperty: " + property + "\n");
				return PrismAPI.run(model, property, true);
			}
		}
		else {
			System.out.println("Verifying Model: " + model.getModelId() + "\nProperty: " + property + "\n");
			HashMap<String, Double> results = new HashMap<String, Double>();
			for (DependencyParameter dep: model.getDependencyParameters()) {
				// get the details of the dependency
				Model depModel = ModelUtils.getModelFromList(dep.getModelID(), models);
				results.put(dep.getName(), solve(depModel, dep.getDefinition()));
			}
			// update the model file to define constants
			ModelUtils.updateModelFileResults(model, results);
			if (context.getPMCEngine() == "PRISM") {
				return PrismAPI.run(model, property, true);
			}
		}
		
		return 0.0;
	}
	
	public double solve(Model model, String property, ArrayList<Model> models) throws FileNotFoundException, PrismException {
		// Base Case is when a mode has no dependencies
		if (model.getDependencyParameters().isEmpty()) {
			System.out.println("Verifying Model: " + model.getModelId() + "\nProperty: " + property + "\n");
			return Solver.prism(model, property);
		}
		else {
			System.out.println("Verifying Dependent Model: " + model.getModelId() + "\nProperty: " + property + "\n");
			HashMap<String, Double> results = new HashMap<String, Double>();
			for (DependencyParameter dep: model.getDependencyParameters()) {
				// get the details of the dependency
				Model depModel = null;
				for (Model m: models) {
					if (dep.getModelID().equals(m.getModelId())) {
						depModel = m;
					}
				}
				results.put(dep.getName(), solve(depModel, dep.getDefinition(), models));
			}
			
			// update the model file to define constants
			ModelUtils.updateModelFileResults(model, results);
			return Solver.prism(model, property);
		}		
	}
}
