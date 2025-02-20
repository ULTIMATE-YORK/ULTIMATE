package utils;

import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class DialogOpener {
	
	public static String openPrismFileDialog(Stage stage) {
	    FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle("Choose a Prism Model File");
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PRISM file extensions", FileUtils.VALID_PRISM_FILE_EXTENSIONS));
	    File selectedFile = fileChooser.showOpenDialog(stage);
	    return (selectedFile != null) ? selectedFile.getAbsolutePath() : null; 
	}
}
