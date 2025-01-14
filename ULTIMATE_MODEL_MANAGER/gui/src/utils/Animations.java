package utils;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class Animations {
	

    public static void fadeInVBox(VBox vbox) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), vbox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    
    // FIXME animations are not working as expected
    public static void fadeOutAndSlideVBox(VBox disappearingVBox, VBox expandingVBox) {
        // Fade out the disappearing VBox
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), disappearingVBox);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // Slide the expanding VBox into the space
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), expandingVBox);
        slideIn.setFromX(expandingVBox.getWidth());
        slideIn.setToX(0);

        // Play animations in parallel
        ParallelTransition parallelTransition = new ParallelTransition(fadeOut, slideIn);
        parallelTransition.play();
    }

}
