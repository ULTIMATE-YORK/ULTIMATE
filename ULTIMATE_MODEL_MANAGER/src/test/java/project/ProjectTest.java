package project;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class ProjectTest {
	
	@Test
	void testValidProject() {
		assertDoesNotThrow(() -> new Project(getResourcePath("projectTestResources/RAD.ultimate")));
	}
	
	@Test
	void testProjectModels() throws IOException {
		Project project = new Project(getResourcePath("projectTestResources/RAD.ultimate"));
		ArrayList<String> modelIDs = project.getModelIDs();
		assertTrue(modelIDs.size() == 3);
		assertTrue(modelIDs.contains("dressing"));
		assertTrue(modelIDs.contains("perceive-user"));
		assertTrue(modelIDs.contains("pick-garment"));
	}

	private String getResourcePath(String resource) {
        URL resourceUrl = getClass().getClassLoader().getResource(resource);
        return Paths.get(resourceUrl.getPath()).toString();
    }
}
