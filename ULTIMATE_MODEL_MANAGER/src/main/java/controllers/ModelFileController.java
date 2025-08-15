
package controllers;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;
import utils.FileUtils;

public class ModelFileController {

    @FXML private TextFlow modelFile;    
    private SharedContext sharedContext = SharedContext.getContext();
    private Project project = SharedContext.getProject();

    @FXML
    public void initialize() {
        setListeners();
        firstcall();
    }

    private void firstcall() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return FileUtils.getFileContent(project.getCurrentModel().getFilePath());
            } catch (IOException e) {
                Alerter.showErrorAlert("Model File not found!", e.getMessage());
                return null;
            }
        }).thenAccept(fileContent -> 
            Platform.runLater(() -> updateTextFlow(fileContent))
        );
    }

    private void setListeners() {
        // Listen for changes to the current model
        project.currentModelProperty().addListener((obs, oldModel, newModel) -> {
            if (newModel != null) {
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return FileUtils.getFileContent(newModel.getFilePath());
                    } catch (IOException e) {
                        Alerter.showErrorAlert("Model File not found!", e.getMessage());
                        return null;
                    }
                }).thenAccept(fileContent -> 
                    Platform.runLater(() -> updateTextFlow(fileContent))
                );
            }
        });
    }



private void updateTextFlow(String fileContent) {
    modelFile.getChildren().clear(); // Clear existing content
    if (fileContent != null) {
        String[] lines = fileContent.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("//")) {
                // Entire line is a comment, make it green
                Text commentText = new Text(line + "\n");
                commentText.setStyle("-fx-fill: green;");
                modelFile.getChildren().add(commentText);
            } else {
                // Process the line word by word
                String[] words = line.split("\\s+");
                for (String word : words) {
                    Text text = new Text(word + " ");
                    if (word.matches("const|int|double|rewards|endrewards|module|endmodule|label|true|init|false")) {
                        text.setStyle("-fx-fill: black; -fx-font-weight: bold;"); // Bold black
                    } else if (word.matches("\\d+(\\.\\d+)?")) { // Matches integers or doubles
                        text.setStyle("-fx-fill: blue;"); // Blue
                    } else if (word.matches("[a-zA-Z0-9]+")) { // Matches alphanumeric
                        text.setStyle("-fx-fill: red;"); // Red
                    } else {
                        text.setStyle("-fx-fill: black;"); // Regular black
                    }
                    modelFile.getChildren().add(text);
                }
                modelFile.getChildren().add(new Text("\n")); // Add a newline after each line
            }
        }
    }
}


}
