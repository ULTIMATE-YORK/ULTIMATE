package project;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import model.Model;

public class ProjectTest {
	
	@Test
	void testValidProject() {
		assertDoesNotThrow(() -> new Project(getResourcePath("projectTestResources/SMD.ultimate")));
	}
	
	@Test
	void testProjectModels() throws IOException {
		Project project = new Project(getResourcePath("projectTestResources/SMD.ultimate"));
		ArrayList<String> modelIDs = project.getModelIDs();
		assertTrue(modelIDs.size() == 2);
		assertTrue(modelIDs.contains("MotionSensor"));
		assertTrue(modelIDs.contains("SmartLighting"));
	}
	
	@Test
	void testAddingDuplicateModel() throws IOException {
		Project project = new Project(getResourcePath("projectTestResources/SMD.ultimate"));
		Model model = new Model(getResourcePath("projectTestResources/MotionSensor.prism"));
		assertThrows(IllegalArgumentException.class, () -> {
			project.addModel(model);
		});
	}
	
	@Test
	void testRemoveModel() throws IOException {
		Project project = new Project(getResourcePath("projectTestResources/SMD.ultimate"));
		Model model = new Model(getResourcePath("projectTestResources/MotionSensor.prism"));
		ArrayList<String> modelIDs1 = project.getModelIDs(); // should be 2
		project.removeModel(model);
		ArrayList<String> modelIDs2 = project.getModelIDs(); // should be 1
		assertTrue(modelIDs1.size() == modelIDs2.size() + 1);
	}

	private String getResourcePath(String resource) {
        URL resourceUrl = getClass().getClassLoader().getResource(resource);
        return Paths.get(resourceUrl.getPath()).toString();
    }
}
