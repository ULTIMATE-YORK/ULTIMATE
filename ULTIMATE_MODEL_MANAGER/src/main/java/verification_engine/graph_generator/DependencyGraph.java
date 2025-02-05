package verification_engine.graph_generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import model.persistent_objects.*;

/**
 * Represents a graph structure used to detect cycles in dependency parameters across models.
 * This class provides utility methods for analysing dependencies.
 */

public class DependencyGraph {
	
	private Graph<String, DefaultEdge> dependencyGraph;
	private ArrayList<Model> models;
	
	public DependencyGraph(ArrayList<Model> models) {
		this.models = models;
		this.dependencyGraph = createDependencyGraph();
	}
	
	private Graph<String, DefaultEdge> createDependencyGraph() {
		Graph<String, DefaultEdge> dependencyGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
		
		// create all the vertices in the graph
		for (Model model : models) {
			dependencyGraph.addVertex(model.getModelId());
		}
		
		// fill in all the edges based on the dependency parameters of each model
		for (Model model : models) {
			// loop through the DependencyParameters of each model
			for (DependencyParameter dp : model.getDependencyParameters()) {
				dependencyGraph.addEdge(model.getModelId(), dp.getModelID());
			}
		}
		
		return dependencyGraph;
	}
	
	public boolean hasCycle() {
		// Detect cycles
        CycleDetector<String, DefaultEdge> cycleDetector = new CycleDetector<>(dependencyGraph);
        return cycleDetector.detectCycles();
	}
	
	public List<Set<String>> getSCCs() {
        
		// Use the StrongConnectivityInspector to find strongly connected components
		KosarajuStrongConnectivityInspector<String, DefaultEdge> sci = new KosarajuStrongConnectivityInspector<>(dependencyGraph);
        List<Set<String>> stronglyConnectedSubgraphs = sci.stronglyConnectedSets();

        // Print the strongly connected components
        for (Set<String> component : stronglyConnectedSubgraphs) {
            System.out.println("Component: " + component);
        }
        
        return stronglyConnectedSubgraphs;
	}
}
