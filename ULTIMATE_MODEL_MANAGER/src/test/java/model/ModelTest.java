package model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import parameters.UncategorisedParameter;

public class ModelTest {
	
    @Test
    void testValidModel() {
        assertDoesNotThrow(() -> new Model(getResourcePath("modelTestResources/c.prism")));
    }
	
    @Test
    void testInvalidModel() {
        // Assert that an IOException is thrown when creating an invalid model
        assertThrows(IOException.class, () -> {
            new Model(getResourcePath("modelTestResources/notAModel.txt"));
        });
    }
	
    @Test
    void testModelID() throws IOException {
    	Model model = new Model(getResourcePath("modelTestResources/c.prism"));
    	assertEquals(model.getModelId(), "c");
    }
    
    @Test
	void testAddUncategorizedParameter() throws IOException {
		Model model = new Model(getResourcePath("modelTestResources/MotionSensor.prism"));
		assertEquals(model.getUncategorisedParameters().size(), 2);
        
		String[] expected = {"pLow", "pMed"};
		for (UncategorisedParameter parameter : model.getUncategorisedParameters()) {
			assertTrue(parameter.getName().equals(expected[0]) || parameter.getName().equals(expected[1]));
		}
	}
	
    private String getResourcePath(String resource) {
        URL resourceUrl = getClass().getClassLoader().getResource(resource);
        return Paths.get(resourceUrl.getPath()).toString();
    }
}
