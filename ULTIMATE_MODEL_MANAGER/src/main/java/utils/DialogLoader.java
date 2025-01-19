package utils;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogLoader {
	
	public static void load(String path2Dialog, String title, Object controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(DialogLoader.class.getResource(path2Dialog));
        loader.setController(controller);
        Parent root = loader.load();
	    // Create a new Stage
	    Stage newStage = new Stage();
	    newStage.initModality(Modality.APPLICATION_MODAL);
	    newStage.setTitle(title);
	    newStage.setScene(new Scene(root));
	    newStage.show();
	}
	
	public static void load(String path2Dialog, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(DialogLoader.class.getResource(path2Dialog));
        Parent root = loader.load();
	    // Create a new Stage
	    Stage newStage = new Stage();
	    newStage.initModality(Modality.APPLICATION_MODAL);
	    newStage.setTitle(title);
	    newStage.setScene(new Scene(root));
	    newStage.show();
	}
}
