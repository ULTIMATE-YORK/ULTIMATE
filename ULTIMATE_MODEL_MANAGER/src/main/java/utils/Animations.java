package utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class Animations {
		// TODO refactor
    public static void animateVBoxExpansion(VBox expandingVBox, VBox shrinkingVBox, double expandingWidthPercent, double shrinkingWidthPercent, String direction) {
        if (direction == "horizontal") {
	    	// Define the target width for both VBox components
	        double totalWidth = expandingVBox.getParent().getLayoutBounds().getWidth(); // GridPane's total width
	        double targetWidth1 = (expandingWidthPercent / 100) * totalWidth;
	        double targetWidth2 = (shrinkingWidthPercent / 100) * totalWidth;
	
	        Timeline timeline = new Timeline(
	        	    new KeyFrame(Duration.ZERO,
	        	        new KeyValue(expandingVBox.prefWidthProperty(), expandingVBox.getWidth()),
	        	        new KeyValue(shrinkingVBox.prefWidthProperty(), shrinkingVBox.getWidth())
	        	    ),
	        	    new KeyFrame(Duration.millis(500),
	        	        new KeyValue(expandingVBox.prefWidthProperty(), targetWidth1),
	        	        new KeyValue(shrinkingVBox.prefWidthProperty(), targetWidth2)
	        	    )
	        	);
	
	        timeline.setOnFinished(event -> {
	            // After animation, ensure shrinkingVBox is invisible and not managed if needed
	            // After animation, adjust visibility
	            if (shrinkingWidthPercent == 0) {
	                shrinkingVBox.setVisible(false);
	            }
	            if (expandingWidthPercent > 0) {
	                expandingVBox.setVisible(true);
	            }
	        });
	
	        timeline.play();
	    }
        else {
	    	// Define the target width for both VBox components
	        double totalWidth = expandingVBox.getParent().getLayoutBounds().getHeight(); // GridPane's total width
	        double targetWidth1 = (expandingWidthPercent / 100) * totalWidth;
	        double targetWidth2 = (shrinkingWidthPercent / 100) * totalWidth;
	
	        Timeline timeline = new Timeline(
	        	    new KeyFrame(Duration.ZERO,
	        	        new KeyValue(expandingVBox.prefHeightProperty(), expandingVBox.getHeight()),
	        	        new KeyValue(shrinkingVBox.prefHeightProperty(), shrinkingVBox.getHeight())
	        	    ),
	        	    new KeyFrame(Duration.millis(500),
	        	        new KeyValue(expandingVBox.prefHeightProperty(), targetWidth1),
	        	        new KeyValue(shrinkingVBox.prefHeightProperty(), targetWidth2)
	        	    )
	        	);
	
	        timeline.setOnFinished(event -> {
	            // After animation, ensure shrinkingVBox is invisible and not managed if needed
	            // After animation, adjust visibility
	            if (shrinkingWidthPercent == 0) {
	                shrinkingVBox.setVisible(false);
	            }
	            if (expandingWidthPercent > 0) {
	                expandingVBox.setVisible(true);
	            }
	        });
	
	        timeline.play();
        }
    }
}
