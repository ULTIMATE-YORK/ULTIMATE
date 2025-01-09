package com.ultimatemodelmanager;

import com.controllers.Controller;
import com.parameters.Model;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Main extends Application {
	   
	@Override
	public void start(Stage stage) throws Exception {
        // Create an ObservableList to hold the models
        ObservableList<Model> models = FXCollections.observableArrayList();
        Stage mainStage = stage;
        
        // Load the FXML file and set the controller
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ultimatemodelmanager/MainView.fxml"));
        
        // Inject models into the controller
        loader.setController(new Controller(mainStage, models));
        
        // Load the FXML root element (GridPane in this case)
        GridPane root = loader.load();

        // Set up the Scene and Stage
        mainStage.setScene(new Scene(root, 1500, 1000));
        mainStage.setTitle("Ultimate Model Manager"); // You can change the title
        mainStage.show();
	}

	public static void main(String[] args) {
        launch(args);
	}
}