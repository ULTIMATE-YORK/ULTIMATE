package main;
import persistent_objects.SharedData;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Main extends Application {
	   
	@Override
	public void start(Stage stage) throws Exception {
        // Set up the shared context
        SharedData sharedContext = SharedData.getInstance();
        sharedContext.setMainStage(stage);
        // Load the FXML file and set the controller
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_view.fxml"));
        
        // Load the FXML root element (GridPane in this case)
        GridPane root = loader.load();

        // Set up the Scene and Stage
        stage.setScene(new Scene(root, 1500, 1000));
        stage.setTitle("Ultimate Model Manager"); // You can change the title

        // Set minimum size for the Stage
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
	}
	
	public static void main(String[] args) {
        launch(args);
	}
}