package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import logging.TextAreaAppender;

public class LogController {
	
	@FXML private TextArea logsField;
	
    @FXML
    public void initialize() {
        // Pass the TextArea to your custom appender.
        TextAreaAppender.setTextArea(logsField);
    }

}
