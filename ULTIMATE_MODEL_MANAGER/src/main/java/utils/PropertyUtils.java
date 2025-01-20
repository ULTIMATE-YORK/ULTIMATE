package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class PropertyUtils {
	
	public static void generateFile(String fileName, ArrayList<String> properties) {
		try (FileWriter writer = new FileWriter(fileName + ".pctl")) {
            for (String str : properties) {
                writer.write(str + ";\n");
            }
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
	}

}
