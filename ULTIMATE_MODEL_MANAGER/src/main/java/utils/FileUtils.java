package utils;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.persistent_objects.Property;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileUtils {

    public static File openFileDialog(Stage stage, String title, String description, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
        return fileChooser.showOpenDialog(stage);
    }
    
    public static File openFileDialog(Stage stage, String title, String description, String[] extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extensions));
        return fileChooser.showOpenDialog(stage);
    }

    public static JSONObject parseJSONFile(File file) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
        return new JSONObject(content);
    }
    
    public static File getFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        File file = new File(fileName);
        
        if (!file.exists()) {
            System.out.println("File does not exist: " + fileName);
        } else {
            System.out.println("File found: " + file.getAbsolutePath());
        }
        
        return file;
    }
    
	public static void generatePropertyFile(String fileName, ArrayList<Property> property) {
		try (FileWriter writer = new FileWriter(fileName)) {
            for (Property prop : property) {
                writer.write(prop.getDefinition() + ";\n");
            }
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
	}
}