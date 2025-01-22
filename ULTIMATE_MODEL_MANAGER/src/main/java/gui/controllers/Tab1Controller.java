package gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class Tab1Controller extends Controller {
	
	@FXML VBox modelsAndPropertiesContainer;
	@FXML GridPane parametersPane;
	@FXML GridPane parentGridPane;
	
	@FXML
	private void initialize() {
		setUpResizing();
	}
	
	private void setUpResizing() {
		modelsAndPropertiesContainer.prefWidthProperty().bind(parentGridPane.widthProperty().multiply(1.0 / 3.0));
		parametersPane.prefWidthProperty().bind(parentGridPane.widthProperty().multiply(2.0 / 3.0));
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerController() {
		// TODO Auto-generated method stub
		
	}
}
