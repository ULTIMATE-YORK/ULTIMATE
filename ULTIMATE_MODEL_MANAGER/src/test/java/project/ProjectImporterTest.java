package project;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.jupiter.api.Test;

import model.Model;

public class ProjectImporterTest {

	@Test
	void testImportProject() throws IOException {
		ProjectImporter importer = new ProjectImporter(getResourcePath("projectTestResources/RAD.ultimate"));
		Set<Model> projectModels = importer.importProject();
	    assertTrue(projectModels.size() == 3);
	}
    
	private String getResourcePath(String resource) {
        URL resourceUrl = getClass().getClassLoader().getResource(resource);
        return Paths.get(resourceUrl.getPath()).toString();
    }
}
