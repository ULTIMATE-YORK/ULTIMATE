package headless;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import model.persistent_objects.Model;

public class ProjectParserTest {
	
	@Test
    void testRAD() throws Exception {
        
		// Arrange: get the resource as a file path from the class path
        String filePath = getClass().getResource("/headless/world-model.json").getPath();
		
        // Act
		ArrayList<Model> testModels = ProjectParser.parse(filePath);
		
		// Assert
		
		assertTrue(testModels.size() == 3);
		
	}
}
