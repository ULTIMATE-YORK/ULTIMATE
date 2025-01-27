package verification_engine.graph_generator;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

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
	public float solve(Model model, String property) throws FileNotFoundException, PrismException {
		// Base Case is when a mode has no dependencies
		if (model.getDependencyParameters().isEmpty()) {
			if (context.getPMCEngine() == "PRISM") {
				return PrismAPI.run(model, property, true);
			}
		}
		else {
			HashMap<String, Float> results = new HashMap<String, Float>();
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
		
		return (float) 0.0;
	}
}
