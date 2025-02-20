package utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

public class PrismFileParserTest {
	
	@Test
	void testParseFile() throws IOException {
		PrismFileParser parser = new PrismFileParser();
		List<String> parameters = parser.parseFile(getResourcePath("utilTestResources/MotionSensor.prism"));
		assertTrue(parameters.size() == 2);
		assertTrue(parameters.contains("pLow"));
		assertTrue(parameters.contains("pMed"));
	}
	
	private String getResourcePath(String resource) {
        URL resourceUrl = getClass().getClassLoader().getResource(resource);
        return Paths.get(resourceUrl.getPath()).toString();
    }
}
