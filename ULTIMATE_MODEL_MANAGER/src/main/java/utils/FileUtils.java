package utils;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.persistent_objects.Model;
import model.persistent_objects.Property;
import javafx.concurrent.Task;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class providing asynchronous file operations to avoid blocking the UI thread in JavaFX applications.
 */
public class FileUtils {

    /**
     * Opens a file dialog asynchronously on the JavaFX thread.
     * 
     * @param stage       The JavaFX stage to associate the dialog with.
     * @param title       The title of the file chooser dialog.
     * @param description The description for the file type filter.
     * @param extension   The allowed file extension (e.g., "*.txt").
     * @param callback    Callback to handle the selected file.
     */
    public static void openFileDialog(Stage stage, String title, String description, String extension, FileCallback callback) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(title);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (callback != null) {
                callback.onFileSelected(selectedFile);
            }
        });
    }

    /**
     * Opens a file dialog with multiple file extensions asynchronously on the JavaFX thread.
     * 
     * @param stage       The JavaFX stage to associate the dialog with.
     * @param title       The title of the file chooser dialog.
     * @param description The description for the file type filter.
     * @param extensions  Array of allowed file extensions.
     * @param callback    Callback to handle the selected file.
     */
    public static void openFileDialog(Stage stage, String title, String description, String[] extensions, FileCallback callback) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(title);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extensions));
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (callback != null) {
                callback.onFileSelected(selectedFile);
            }
        });
    }

    /**
     * Opens a directory chooser dialog asynchronously on the JavaFX thread.
     * 
     * @param stage    The JavaFX stage to associate the dialog with.
     * @param title    The title of the directory chooser dialog.
     * @param callback Callback to handle the selected directory.
     */
    public static void openDirectoryDialog(Stage stage, String title, FileCallback callback) {
        Platform.runLater(() -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle(title);
            File selectedDirectory = dirChooser.showDialog(stage);
            if (callback != null) {
                callback.onFileSelected(selectedDirectory);
            }
        });
    }

    /**
     * Parses a JSON file asynchronously without blocking the UI thread.
     * 
     * @param file     The JSON file to parse.
     * @param callback Callback to handle the parsed JSONObject or error.
     */
    public static void parseJSONFileAsync(File file, JSONCallback callback) {
        Task<JSONObject> task = new Task<>() {
            @Override
            protected JSONObject call() throws Exception {
                String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
                return new JSONObject(content);
            }
        };

        task.setOnSucceeded(event -> callback.onJSONParsed(task.getValue()));
        task.setOnFailed(event -> callback.onError(task.getException()));

        new Thread(task).start();
    }

    /**
     * Asynchronously checks for file existence and invokes the callback on the JavaFX thread.
     * 
     * @param fileName The file name to check.
     * @param callback Callback to handle the file result.
     */
    public static void getFileAsync(String fileName, FileCallback callback) {
        CompletableFuture.supplyAsync(() -> {
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new IllegalArgumentException("Filename cannot be null or empty");
            }
            File file = new File(fileName);
            return file;
        }).thenAccept(file -> Platform.runLater(() -> callback.onFileSelected(file)));
    }

    /**
     * Writes properties to a file asynchronously to prevent blocking the UI thread.
     * 
     * @param fileName   The name of the file to write to.
     * @param properties The list of properties to write.
     * @param callback   Callback to handle success or failure.
     */
    public static void generatePropertyFileAsync(String fileName, ArrayList<Property> properties, TaskCallback callback) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (FileWriter writer = new FileWriter(fileName)) {
                    for (Property prop : properties) {
                        writer.write(prop.getDefinition() + ";\n");
                    }
                }
                return null;
            }
        };

        task.setOnSucceeded(event -> callback.onSuccess());
        task.setOnFailed(event -> callback.onError(task.getException()));

        new Thread(task).start();
    }

    /**
     * Updates the property file of a given model asynchronously.
     * 
     * @param model    The model containing the properties to update.
     * @param callback Callback to handle success or failure.
     */
    public static void updatePropertyFileAsync(Model model, TaskCallback callback) {
        generatePropertyFileAsync(model.getPropFile(), model.getProperties(), callback);
    }

    /**
     * Reads properties from a file asynchronously without blocking the UI thread.
     * 
     * @param filepath The path of the property file.
     * @param callback Callback to handle the loaded properties or an error.
     */
    public static void getPropertiesFromFileAsync(String filepath, PropertiesCallback callback) {
        Task<ArrayList<Property>> task = new Task<>() {
            @Override
            protected ArrayList<Property> call() throws Exception {
                Path path = Paths.get(filepath);
                List<String> allLines = Files.readAllLines(path);
                ArrayList<Property> props = new ArrayList<>();
                for (String s : allLines) {
                    props.add(new Property(s));
                }
                return props;
            }
        };

        task.setOnSucceeded(event -> callback.onPropertiesLoaded(task.getValue()));
        task.setOnFailed(event -> callback.onError(task.getException()));

        new Thread(task).start();
    }

    // Functional interfaces for callback-based asynchronous operations

    /**
     * Callback interface for file selection operations.
     */
    public interface FileCallback {
        void onFileSelected(File file);
    }

    /**
     * Callback interface for JSON parsing operations.
     */
    public interface JSONCallback {
        void onJSONParsed(JSONObject json);
        void onError(Throwable e);
    }

    /**
     * Callback interface for general asynchronous tasks.
     */
    public interface TaskCallback {
        void onSuccess();
        void onError(Throwable e);
    }

    /**
     * Callback interface for loading properties from a file.
     */
    public interface PropertiesCallback {
        void onPropertiesLoaded(ArrayList<Property> properties);
        void onError(Throwable e);
    }
}