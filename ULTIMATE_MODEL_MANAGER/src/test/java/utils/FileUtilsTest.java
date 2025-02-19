package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import model.Model;

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
    void testRemoveFileExtension() throws IOException {
    	assertEquals(FileUtils.removeFileExtension(getResourcePath("utilTestResources/c.prism")), "c");
    	assertEquals(FileUtils.removeFileExtension(getResourcePath("utilTestResources/c.ctmc")), "c");
    	assertEquals(FileUtils.removeFileExtension(getResourcePath("utilTestResources/c.dtmc")), "c");
    	assertEquals(FileUtils.removeFileExtension(getResourcePath("utilTestResources/c.mdp")), "c");
    	assertEquals(FileUtils.removeFileExtension(getResourcePath("utilTestResources/c.pomdp")), "c");
    }
    
    @Test
    void testNoFile() {
        assertFalse(FileUtils.isFile("noFile.txt"));
    }

    private String getResourcePath(String resource) {
        URL resourceUrl = getClass().getClassLoader().getResource(resource);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resource);
        }
        return Paths.get(resourceUrl.getPath()).toString();
    }
}
