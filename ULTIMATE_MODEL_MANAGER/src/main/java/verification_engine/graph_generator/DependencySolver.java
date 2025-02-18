package verification_engine.graph_generator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import headless.Solver;
import model.persistent_objects.DependencyParameter;
import model.persistent_objects.Model;
import prism.PrismException;
import utils.ModelUtils;
import verification_engine.storm.StormAPI;

public class DependencySolver {
	
	public double solve(Model model, String property, ArrayList<Model> models) throws FileNotFoundException, PrismException {
		// Base Case is when a mode has no dependencies
		if (model.getDependencyParameters().isEmpty()) {
			return Solver.prism(model, property);
		}
		else {
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
	
	public double solveStorm(Model model, String property, ArrayList<Model> models, String stormInstallation) {
		// Base Case is when a mode has no dependencies
		if (model.getDependencyParameters().isEmpty()) {
			return StormAPI.run(model, property, stormInstallation);
		}
		else {
			HashMap<String, Double> results = new HashMap<String, Double>();
			for (DependencyParameter dep: model.getDependencyParameters()) {
				// get the details of the dependency
				Model depModel = null;
				for (Model m: models) {
					if (dep.getModelID().equals(m.getModelId())) {
						depModel = m;
					}
				}
				System.out.println(dep.getDefinition());
				results.put(dep.getName(), StormAPI.run(depModel, dep.getDefinition(), stormInstallation));
			}
			
			// update the model file to define constants
			ModelUtils.updateModelFileResults(model, results);
			return StormAPI.run(model, property, stormInstallation);
		}	
	}
}
