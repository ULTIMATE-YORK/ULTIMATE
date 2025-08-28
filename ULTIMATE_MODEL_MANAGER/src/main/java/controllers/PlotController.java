package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import project.Project;
import sharedContext.SharedContext;


// TODO: safely delete (or implement)
public class PlotController {
	
	@FXML private ScrollPane chartHolder;
	@FXML private Button generatePlotsButton;
	
    private SharedContext sharedContext = SharedContext.getContext();
    private Project project = SharedContext.getProject();
	
	@FXML
	private void generatePlots() {
	}

}
