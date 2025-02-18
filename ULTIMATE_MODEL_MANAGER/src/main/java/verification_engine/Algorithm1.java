package verification_engine;

import java.util.ArrayList;

import model.persistent_objects.Model;
import verification_engine.graph_generator.DependencyGraph;
import verification_engine.graph_generator.DependencySolver;

public class Algorithm1 {
	
	public Double Verify(Model model, String property, ArrayList<Model> models) {
		return 0.0;
	}
	
	public static Double Verify(Model model, String property, ArrayList<Model> models, String stormInstallation) {
		DependencySolver ds = new DependencySolver();
		DependencyGraph dg = new DependencyGraph(models);
		if (!dg.inSCC(model)) {
			return ds.solveStorm(model, property, models, stormInstallation);
		}
		else {
			System.out.println("Model is in a cycle");
			return 0.0;
		}
	}
	
}
