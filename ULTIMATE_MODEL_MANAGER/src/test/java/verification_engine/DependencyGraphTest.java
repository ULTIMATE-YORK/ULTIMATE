package verification_engine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

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
}