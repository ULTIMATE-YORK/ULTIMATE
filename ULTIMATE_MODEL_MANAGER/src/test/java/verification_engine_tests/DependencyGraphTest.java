package verification_engine_tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import verification_engine.graph_generator.DependencyGraph;
import model.persistent_objects.*;

class DependencyGraphTest {

    @Test
    void testNoCycles() {
        // Arrange
        ArrayList<Model> models = new ArrayList<>();

        // Create Models without circular dependencies
        Model modelA = new Model("A", null);
        modelA.addDependencyParameter("", "B" , "");
        Model modelB = new Model("B", null);
        modelB.addDependencyParameter("", "C" , "");
        Model modelC = new Model("C", null);
        
        models.add(modelA);
        models.add(modelB);
        models.add(modelC);

        // Act
        DependencyGraph graph = new DependencyGraph(models);

        // Assert
        assertFalse(graph.hasCycle());
    }

    @Test
    void testWithCycles() {
        // Arrange
        ArrayList<Model> models = new ArrayList<>();

        // Create Models without circular dependencies
        Model modelA = new Model("A", null);
        modelA.addDependencyParameter("", "B" , "");
        Model modelB = new Model("B", null);
        modelB.addDependencyParameter("", "C" , "");
        Model modelC = new Model("C", null);
        modelC.addDependencyParameter("", "A" , "");
        
        models.add(modelA);
        models.add(modelB);
        models.add(modelC);

        // Act
        DependencyGraph graph = new DependencyGraph(models);

        // Assert
        assertTrue(graph.hasCycle());
    }

    @Test
    void testEmptyGraph() {
        // Arrange
        ArrayList<Model> models = new ArrayList<>();

        // Act
        DependencyGraph graph = new DependencyGraph(models);

        // Assert
        assertFalse(graph.hasCycle());
    }
    
    @Test
    void testSCC() {
    	// Arrange
    	ArrayList<Model> models = new ArrayList<Model>();
    	
    	Model modelb = new Model("modelb", null);
    	modelb.addDependencyParameter("b2c", "modelc", null);
    	models.add(modelb);
    	
    	Model modelc = new Model("modelc", null);
    	modelc.addDependencyParameter("c2d", "modeld", null);
    	modelc.addDependencyParameter("c2g", "modelg", null);
    	models.add(modelc);
    	
    	Model modeld = new Model("modeld", null);
    	modeld.addDependencyParameter("d2c", "modelc", null);
    	modeld.addDependencyParameter("d2h", "modelh", null);
    	models.add(modeld);
    	
    	Model modelh = new Model("modelh", null);
    	modelh.addDependencyParameter("h2d", "modeld", null);
    	modelh.addDependencyParameter("h2g", "modelg", null);
    	models.add(modelh);
    	
    	Model modelg = new Model("modelg", null);
    	models.add(modelg);
    	
    	// Act
    	DependencyGraph dg = new DependencyGraph(models);
    	List<Set<String>> scc = dg.getSCC();
    	
        // Define expected strongly connected components
        Set<String> expected1 = new HashSet<>(Arrays.asList("modelb"));
        Set<String> expected2 = new HashSet<>(Arrays.asList("modelc", "modeld", "modelh"));
        Set<String> expected3 = new HashSet<>(Arrays.asList("modelg"));

        // Assert that the number of components is as expected.
        assertEquals(3, scc.size(), "There should be exactly 3 strongly connected components.");

        // Assert that each expected component is present in the SCC list.
        // Since sets implement equals() correctly, we can use contains().
        assertTrue(scc.contains(expected1), "SCC does not contain expected component: " + expected1);
        assertTrue(scc.contains(expected2), "SCC does not contain expected component: " + expected2);
        assertTrue(scc.contains(expected3), "SCC does not contain expected component: " + expected3);
    }
}