package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class FileUtilsTest {
	
    @Test
    void testIsPrismFile() throws IOException {
        // Check if a file is a prism file
        assertTrue(FileUtils.isPrismFile(getResourcePath("utilTestResources/c.prism")));
        assertTrue(FileUtils.isPrismFile(getResourcePath("utilTestResources/c.ctmc")));
        assertTrue(FileUtils.isPrismFile(getResourcePath("utilTestResources/c.dtmc")));
        assertTrue(FileUtils.isPrismFile(getResourcePath("utilTestResources/c.mdp")));
        assertTrue(FileUtils.isPrismFile(getResourcePath("utilTestResources/c.pomdp")));
    }
    
    @Test
    void testRemovePrismFileExtension() throws IOException {
    	assertEquals(FileUtils.removePrismFileExtension(getResourcePath("utilTestResources/c.prism")), "c");
    	assertEquals(FileUtils.removePrismFileExtension(getResourcePath("utilTestResources/c.ctmc")), "c");
    	assertEquals(FileUtils.removePrismFileExtension(getResourcePath("utilTestResources/c.dtmc")), "c");
    	assertEquals(FileUtils.removePrismFileExtension(getResourcePath("utilTestResources/c.mdp")), "c");
    	assertEquals(FileUtils.removePrismFileExtension(getResourcePath("utilTestResources/c.pomdp")), "c");
    }
    
    @Test
    void testRemoveUltimateFileExtension() throws IOException {
    	assertEquals(FileUtils.removeUltimateFileExtension(getResourcePath("utilTestResources/empty.ultimate")), "empty");
    }
    
    @Test
    void testNoFile() {
        assertFalse(FileUtils.isFile("noFile.txt"));
    }
    
    @Test
	void testIsUltimateFile() throws IOException {
		assertTrue(FileUtils.isUltimateFile(getResourcePath("utilTestResources/empty.ultimate")));
	}
    
    @Test
	void testNotUltimateFile() {
		// Assert that an IOException is thrown when checking if a non-existent file is
		// an ultimate file
		assertThrows(IOException.class, () -> {
			FileUtils.isUltimateFile(getResourcePath("utilTestResources/notAFile.txt"));
		});
	}

    private String getResourcePath(String resource) {
        URL resourceUrl = getClass().getClassLoader().getResource(resource);
        return Paths.get(resourceUrl.getPath()).toString();
    }
}
