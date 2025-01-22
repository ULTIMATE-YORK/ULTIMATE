package gui.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import model.persistent_objects.SharedData;

public class Tab2 extends Controller {
	
	@FXML private Tab logs_tab;
	@FXML private GridPane parentGridPane;
	@FXML private HBox logsContainer;
	@FXML private TextArea logsField;
	
	private SharedData context;
	
	@FXML
	private void initialize() {
	    // Fetch shared data from the SharedContext
        context = SharedData.getInstance();
        registerController();
	}

	public void updateLogs() {
        File logFile = new File("logs.txt");

        if (!logFile.exists()) {
            System.out.println("Log file does not exist.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");  // Read and append line by line
            }
            // Update TextArea on the JavaFX UI thread
            // TODO move all UI updated to thread!!
            Platform.runLater(() -> logsField.appendText(content.toString()));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	@Override
	public void registerController() {
		context.registerController(this);	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}
	
}
