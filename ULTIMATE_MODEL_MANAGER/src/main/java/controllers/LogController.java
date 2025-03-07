package controllers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import logging.TextFlowAppender;
import sharedContext.SharedContext;
import utils.DialogOpener;

public class LogController {
	
	@FXML private TextFlow logsField;
	@FXML private Button saveLogs;
	
    private SharedContext sharedContext = SharedContext.getInstance();
	
    @FXML
    private void initialize() {
        // Pass the TextArea to your custom appender.
        TextFlowAppender.setTextFlow(logsField);
    }
    
    @FXML
    private void saveLogs() {
    	String filePath = DialogOpener.openLogsSaveDialog(sharedContext.getMainStage());
    	if (filePath == null) {
    		return;
    	}
        StringBuilder textContent = new StringBuilder();
        for (var node : logsField.getChildren()) {
            if (node instanceof Text) {
                textContent.append(((Text) node).getText());
            }
        }
        writeTextToFile(filePath, textContent.toString());
        
    }
    
    private void writeTextToFile(String filePath, String content) {
        try {
            Files.writeString(Path.of(filePath), content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
