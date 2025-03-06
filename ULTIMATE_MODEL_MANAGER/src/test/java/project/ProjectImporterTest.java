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
		ProjectImporter importer = new ProjectImporter(getResourcePath("projectTestResources/SMD.ultimate"));
		Set<Model> projectModels = importer.importProject();
	    assertTrue(projectModels.size() == 2);
	}
	
	/*
	@Test
	void testDeserializeParameters() throws IOException {
		ProjectImporter importer = new ProjectImporter(getResourcePath("projectTestResources/RAD.ultimate"));
		Set<Model> projectModels = importer.importProject();
		
		projectModels.forEach(model -> {
			if (model.getModelId().equals("dressing")) {
				assertTrue(model.getDependencyParameters().size() == 0);
				assertTrue(model.getExternalParameters().size() == 0);
				assertTrue(model.getInternalParameters().size() == 0);
			}
		});
	}
    **/
	private String getResourcePath(String resource) {
        URL resourceUrl = getClass().getClassLoader().getResource(resource);
        return Paths.get(resourceUrl.getPath()).toString();
    }
}
