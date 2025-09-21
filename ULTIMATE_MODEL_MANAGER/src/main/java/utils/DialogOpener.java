package utils;

import java.io.File;
import java.io.IOException;

import controllers.EditDependencyParameter;
import controllers.EditExternalParameter;
import controllers.EditInternalParameter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import parameters.DependencyParameter;
import parameters.ExternalParameter;
import parameters.InternalParameter;

public class DialogOpener {

	private static File lastDir;

	private static File getInitialDir() {
		File file = null;
		if (lastDir != null) {
			file = lastDir;
		} else {
			File ultDir = new File(System.getenv("ULTIMATE_DIR"));
			if (ultDir.exists()) {
				file = ultDir;
			}
		}
		return file;
	}

	private static void updateInitialDir(File selectedDir) {
		if (selectedDir != null) {
			lastDir = selectedDir.getParentFile();
		}
	}

	public static String openPrismFileDialog(Stage stage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose a Prism Model File");
		fileChooser.getExtensionFilters()
				.add(new FileChooser.ExtensionFilter("PRISM file extensions", FileUtils.VALID_PRISM_FILE_EXTENSIONS));
		fileChooser.setInitialDirectory(getInitialDir());
		File selectedFile = fileChooser.showOpenDialog(stage);
		updateInitialDir(selectedFile);
		return (selectedFile != null) ? selectedFile.getAbsolutePath() : null;
	}

	public static String openUltimateFileDialog(Stage stage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose an ULTIMATE File");
		fileChooser.getExtensionFilters()
				.add(new FileChooser.ExtensionFilter("ULTIMATE file extensions", FileUtils.VALID_ULT_FILE_EXTENSIONS));
		fileChooser.setInitialDirectory(getInitialDir());
		File selectedFile = fileChooser.showOpenDialog(stage);
		updateInitialDir(selectedFile);
		return (selectedFile != null) ? selectedFile.getAbsolutePath() : null;
	}

	public static String openUltimateSaveDialog(Stage stage) {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save File");
		fileChooser.getExtensionFilters()
				.add(new FileChooser.ExtensionFilter("Ultimate files (*.ultimate)", "*.ultimate"));
		fileChooser.setInitialDirectory(getInitialDir());
		File selectedFile = fileChooser.showSaveDialog(stage);
		updateInitialDir(selectedFile);
		if (selectedFile == null) {
			return null;
		} else if (!selectedFile.getAbsolutePath().toLowerCase().endsWith(".ultimate")) {
			return selectedFile.getAbsolutePath() + ".ultimate";
		} else {
			return selectedFile.getAbsolutePath();
		}
	}

	public static String openDataSaveDialog(Stage stage, String defaultFileName) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save File");
		fileChooser.setInitialDirectory(getInitialDir());
		fileChooser.setInitialFileName(defaultFileName);
		File selectedFile = fileChooser.showSaveDialog(stage);
		updateInitialDir(selectedFile);
		return (selectedFile != null) ? selectedFile.getAbsolutePath() : null;
	}

	public static String openLogsSaveDialog(Stage stage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save File");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ultimate Log files (*.ulog)", "*.ulog"));
		fileChooser.setInitialDirectory(getInitialDir());
		File selectedFile = fileChooser.showSaveDialog(stage);
		updateInitialDir(selectedFile);
		return (selectedFile != null) ? selectedFile.getAbsolutePath() : null;
	}

	public static void openDialogWindow(Stage ownerStage, String path, String title) throws IOException {
		FXMLLoader loader = new FXMLLoader(DialogOpener.class.getResource(path));
		Parent root = loader.load();
		// Create a new Stage
		Stage dialogStage = new Stage();
		// Set the owner of the dialog stage to the passed stage.
		dialogStage.initOwner(ownerStage);
		// Use APPLICATION_MODAL to block events to the owner until this dialog is
		// closed.
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.setTitle(title);
		dialogStage.setScene(new Scene(root));
		dialogStage.showAndWait(); // Alternatively, show() if you don't need to wait.
	}

	/*
	 * Overloaded method to allow the passing of a dependency Parameter
	 */
	public static void openDialogWindow(Stage ownerStage, String path, String title, DependencyParameter dp)
			throws IOException {
		FXMLLoader loader = new FXMLLoader(DialogOpener.class.getResource(path));
		Parent root = loader.load();
		EditDependencyParameter controller = loader.getController();
		controller.setDP(dp);
		// Create a new Stage
		Stage dialogStage = new Stage();
		// Set the owner of the dialog stage to the passed stage.
		dialogStage.initOwner(ownerStage);
		// Use APPLICATION_MODAL to block events to the owner until this dialog is
		// closed.
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.setTitle(title);
		dialogStage.setScene(new Scene(root));
		dialogStage.showAndWait(); // Alternatively, show() if you don't need to wait.
	}

	/*
	 * Overloaded method to allow the passing of a external Parameter
	 */
	public static void openDialogWindow(Stage ownerStage, String path, String title, ExternalParameter ep)
			throws IOException {
		FXMLLoader loader = new FXMLLoader(DialogOpener.class.getResource(path));
		Parent root = loader.load();
		EditExternalParameter controller = loader.getController();
		controller.setEP(ep);
		// Create a new Stage
		Stage dialogStage = new Stage();
		// Set the owner of the dialog stage to the passed stage.
		dialogStage.initOwner(ownerStage);
		// Use APPLICATION_MODAL to block events to the owner until this dialog is
		// closed.
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.setTitle(title);
		dialogStage.setScene(new Scene(root));
		dialogStage.showAndWait(); // Alternatively, show() if you don't need to wait.
	}

	/*
	 * Overloaded method to allow the passing of a internal Parameter
	 */
	public static void openDialogWindow(Stage ownerStage, String path, String title, InternalParameter ip)
			throws IOException {
		FXMLLoader loader = new FXMLLoader(DialogOpener.class.getResource(path));
		Parent root = loader.load();
		EditInternalParameter controller = loader.getController();
		controller.setIP(ip);
		// Create a new Stage
		Stage dialogStage = new Stage();
		// Set the owner of the dialog stage to the passed stage.
		dialogStage.initOwner(ownerStage);
		// Use APPLICATION_MODAL to block events to the owner until this dialog is
		// closed.
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.setTitle(title);
		dialogStage.setScene(new Scene(root));
		dialogStage.showAndWait(); // Alternatively, show() if you don't need to wait.
	}

}
