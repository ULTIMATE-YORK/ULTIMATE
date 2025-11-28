package utils;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import sharedContext.SharedContext;

public class Alerter {

	public static void showErrorAlert(String title, String message) {
		if (SharedContext.getMainStage() != null) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(message);
			alert.showAndWait();
		} else {
			System.out.println("Error Alert: " + title + ": " + message);
		}
	}

	public static void showInfoAlert(String title, String message) {
		if (SharedContext.getMainStage() != null) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(message);
			alert.showAndWait();
		} else {
			System.out.println("Info Alert: " + title + ": " + message);
		}
	}

	public static boolean showConfirmationAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		Optional<ButtonType> result = alert.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK;
	}

	public static void showWarningAlert(String title, String message) {
		if (SharedContext.getMainStage() != null) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(message);
			alert.showAndWait();
		} else {
			System.out.println("Warning Alert: " + title + ": " + message);
		}
	}
	
	public static String showSaveBeforeActionAlert(String title, String message){
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		
		ButtonType saveButton = new ButtonType("Save");
		ButtonType dontSaveButton = new ButtonType("Don't Save");
		ButtonType cancelButton = new ButtonType("Cancel");
		
		alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);
		
		Optional<ButtonType> result = alert.showAndWait();
		
		if (result.isPresent()){
			if (result.get() == saveButton){
				return "SAVE";
			}else if (result.get() == dontSaveButton){
				return "DONT_SAVE";
			}
		}
		return "CANCEL";
	}
}
