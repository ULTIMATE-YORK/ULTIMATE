package controllers;

import java.util.HashMap;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public class PlotController {
	
	@FXML private ScrollPane chartHolder;
	@FXML private Button generatePlotsButton;
	
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();
	
	@FXML
	private void generatePlots() {
	}

}
